package actors

import adapters._
import adapters.messages._
import akka.actor.{Actor, ActorRef, Props}
import controllers.WebSocketController
import models.ModelDAO
import play.api.libs.json._
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object WebSocketActor {

  private var usersColl: ModelDAO = _
  private var tablesColl: ModelDAO = _

  def props(client: ActorRef, usersCollection: ModelDAO, tablesCollection: ModelDAO)
           (implicit ex: ExecutionContext): Props = {
    usersColl = usersCollection
    tablesColl = tablesCollection

    Props(new WebSocketActor(client))
  }
}

class WebSocketActor(client: ActorRef)
                    (implicit ex: ExecutionContext) extends Actor {

  import WebSocketActor._

  private var clientIsAuthenticated: Boolean = false
  private var clientIsSubscribed: Boolean = false
  private var clientIsAdmin: Boolean = false

  def handleMessage(msg: ClientMessage): JsValue = msg match {
    // Available to everyone
    case msg: MessageLogin => handleMessageLogin(msg)

    // Available to authorised clients
    case msg: ClientMessage if !clientIsAuthenticated
                                  => MessageError("not_authorized")
    case msg: MessagePing         => handleMessagePing(msg)
    case msg: MessageSubscribe    => handleMessageSubscribe(msg)
    case msg: MessageUnSubscribe  => handleMessageUnSubscribe(msg)
    case msg: MessageError        => msg

    // From subscription actor, notifications
    case msg: MessageTableAdded   => msg
    case msg: MessageTableUpdated => msg
    case msg: MessageTableRemoved => msg

    // Available only to admins
    case msg: ClientMessage if !clientIsAdmin
                                  => MessageError("not_authorized")
    case msg: MessageAddTable     => handleMessageAddTable(msg)
    case msg: MessageUpdateTable  => handleMessageUpdateTable(msg)
    case msg: MessageRemoveTable  => handleMessageRemoveTable(msg)
  }

  def receive: PartialFunction[Any, Unit] = {
    case clientMessage: JsValue =>
      val response: JsValue = handleMessage(clientMessage)
      client ! response
  }

  def handleMessagePing(msg: MessagePing): JsValue = {
    MessagePong(MessagePong.MSG_TYPE, msg.seq)
  }

  def handleMessageLogin(msg: MessageLogin): JsValue = {
    val findFuture: Future[Option[User]] =
      usersColl.findOne[User](selector = Json.obj("username" -> msg.username))

    Await.result(findFuture, 3 seconds) match {
      case Some(u) =>                     // username from req is present in db
        if (u.password == msg.password) { // password is true
          clientIsAuthenticated = true
          if (u.user_type == "admin")     // check user role
            clientIsAdmin = true
          else
            clientIsAdmin = false
          MessageLoginSuccessful(MessageLoginSuccessful.MSG_TYPE, u.user_type)
        }
        else {                            // password is false
          clientIsAuthenticated = false
          MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
        }
      case None =>                        // username from req is NOT present in db
        clientIsAuthenticated = false
        MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
    }
  }

  def handleMessageSubscribe(msg: MessageSubscribe): JsValue = {
    import SubscriptionActor._

    WebSocketController.getSubscriptionActor ! Subscribe(client)
    clientIsSubscribed = true

    val findFuture: Future[List[Table]] = // find tables for response
      tablesColl.find[Table]()

    Await.result(findFuture, 3 seconds) match {
      case tables: List[Table] =>         // tables are present in db
        MessageTableList(MessageTableList.MSG_TYPE, tables)
      case _ =>                           // no tables in db
        MessageTableList(MessageTableList.MSG_TYPE, List())
    }
  }

  def handleMessageUnSubscribe(msg: MessageUnSubscribe): JsValue = {
    import SubscriptionActor._

    WebSocketController.getSubscriptionActor ! UnSubscribe(client)
    clientIsSubscribed = false

    MessageUnSubscribeDone(MessageUnSubscribeDone.MSG_TYPE)
  }

  def handleMessageAddTable(msg: MessageAddTable): JsValue = {
    val updateFuture: Future[UpdateWriteResult] =       // shift the tables that follow the new
      tablesColl.update(
        selector = Json.obj("id" -> Json.obj("$gt" -> msg.after_id)),
        updater = Json.obj("$inc" -> Json.obj("id" -> 1)),
        multi = true)

    Await.result(updateFuture, 3 seconds) match {
      case updateRes: WriteResult =>
        if (updateRes.ok) {
          val insertFuture = tablesColl.insert[Table](  // insert the new table to the vacant place
            that = Table(msg.after_id + 1, msg.table.name, msg.table.participants))

          Await.result(insertFuture, 3 second) match {
            case insertRes: WriteResult =>
              if (insertRes.ok) {
                val successMessage = MessageTableAdded(MessageTableAdded.MSG_TYPE, msg.after_id,
                  Table(msg.after_id + 1, msg.table.name, msg.table.participants))

                import actors.SubscriptionActor._

                WebSocketController.getSubscriptionActor ! Broadcast(successMessage)
                successMessage
              }
              else
                MessageAddTableFailed(MessageAddTableFailed.MSG_TYPE, msg.after_id)
          }
        }
        else
          MessageAddTableFailed(MessageAddTableFailed.MSG_TYPE, msg.after_id)
    }
  }

  def handleMessageUpdateTable(msg: MessageUpdateTable): JsValue = {
    import adapters.ClientMessage._

    val updateFuture: Future[UpdateWriteResult] = // update table by id
      tablesColl.update[JsObject, JsObject](Json.obj("id" -> msg.table.id),
        Json.obj("$set" -> msg.table))

    Await.result(updateFuture, 3 seconds) match {
      case updateRes: WriteResult =>
        if (updateRes.ok) {
          val updateSuccessMessage =
            MessageTableUpdated(MessageTableUpdated.MSG_TYPE, msg.table)

          import actors.SubscriptionActor._

          WebSocketController.getSubscriptionActor ! Broadcast(updateSuccessMessage)
          updateSuccessMessage
        }
        else
          MessageUpdateTableFailed(MessageUpdateTableFailed.MSG_TYPE)
    }
  }

  def handleMessageRemoveTable(msg: MessageRemoveTable) = {
    val removeFuture = tablesColl.remove[JsObject](Json.obj("id" -> msg.id)) // remove table by id

    Await.result(removeFuture, 3 seconds) match {
      case updateRes: WriteResult =>
        if (updateRes.ok) {
          val removeSuccessMess = MessageTableRemoved(MessageTableRemoved.MSG_TYPE, msg.id)

          import actors.SubscriptionActor._

          WebSocketController.getSubscriptionActor ! Broadcast(removeSuccessMess)
          removeSuccessMess
        }
        else
          MessageRemoveTableFailed(MessageRemoveTableFailed.MSG_TYPE, msg.id)
    }
  }
}
