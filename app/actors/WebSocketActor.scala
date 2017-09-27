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

  def props(client: ActorRef, usersCollection: ModelDAO, tablesCollection: ModelDAO)
           (implicit ex: ExecutionContext): Props = {
    Props(new WebSocketActor(client, usersCollection, tablesCollection))
  }
}

class WebSocketActor(client: ActorRef, usersCollection: ModelDAO, tablesCollection: ModelDAO)
                    (implicit ex: ExecutionContext) extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    case clientMessage: JsValue =>
      client ! handleMessageFromNotAuth(clientMessage)
  }

  def receiveAuth: PartialFunction[Any, Unit] = {
    case clientMessage: JsValue =>
      client ! handleMessageFromAuth(clientMessage)
  }

  def receiveAdmin: PartialFunction[Any, Unit] = {
    case clientMessage: JsValue =>
      client ! handleMessageFromAdmin(clientMessage)
  }

  def handleMessageFromNotAuth(msg: ClientMessage): JsValue = msg match {
    case msg: MessageLogin => handleLogin(msg)
    // Error handling
    case msg: MessageError => msg
    case msg: ClientMessage => MessageError("not_authorized")
  }

  def handleMessageFromAuth(msg: ClientMessage): JsValue = msg match {
    case msg: MessagePing         => handlePing(msg)
    case msg: MessageSubscribe    => handleSubscribe(msg)
    case msg: MessageUnSubscribe  => handleUnSubscribe(msg)
    // Another message type or error
    case msg: ClientMessage       => handleMessageFromNotAuth(msg)
  }

  def handleMessageFromAdmin(msg: ClientMessage): JsValue = msg match {
    case msg: MessageAddTable     => handleAddTable(msg)
    case msg: MessageUpdateTable  => handleUpdateTable(msg)
    case msg: MessageRemoveTable  => handleRemoveTable(msg)
    // Another message type or error
    case msg: ClientMessage       => handleMessageFromAuth(msg)
  }

  def handlePing(msg: MessagePing): JsValue = {
    MessagePong(MessagePong.MSG_TYPE, msg.seq)
  }

  def handleLogin(msg: MessageLogin): JsValue = {
    val findFuture: Future[Option[User]] =
      usersCollection.findOne[User](selector = Json.obj("username" -> msg.username))

    Await.result(findFuture, 3 seconds) match {
      case Some(u) =>                     // username from req is present in db
        if (u.password == msg.password) { // password is true
          context.become(receiveAuth)
          if (u.user_type == "admin")     // check user role
            context.become(receiveAdmin)
          MessageLoginSuccessful(MessageLoginSuccessful.MSG_TYPE, u.user_type)
        }
        else {                            // password is false
          MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
        }
      case None =>                        // username from req is NOT present in db
        MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
    }
  }

  def handleSubscribe(msg: MessageSubscribe): JsValue = {
    import SubscriptionActor._

    WebSocketController.getSubscriptionActor ! Subscribe(client)

    val findFuture: Future[List[Table]] = // find tables for response
      tablesCollection.find[Table]()

    Await.result(findFuture, 3 seconds) match {
      case tables: List[Table] =>         // tables are present in db
        MessageTableList(MessageTableList.MSG_TYPE, tables)
      case _ =>                           // no tables in db
        MessageTableList(MessageTableList.MSG_TYPE, List())
    }
  }

  def handleUnSubscribe(msg: MessageUnSubscribe): JsValue = {
    import SubscriptionActor._

    WebSocketController.getSubscriptionActor ! UnSubscribe(client)
    MessageUnSubscribeDone(MessageUnSubscribeDone.MSG_TYPE)
  }

  def handleAddTable(msg: MessageAddTable): JsValue = {
    val updateFuture: Future[UpdateWriteResult] =       // shift the tables that follow the new
      tablesCollection.update(
        selector = Json.obj("id" -> Json.obj("$gt" -> msg.after_id)),
        updater = Json.obj("$inc" -> Json.obj("id" -> 1)),
        multi = true)

    Await.result(updateFuture, 3 seconds) match {
      case updateRes: WriteResult =>
        if (updateRes.ok) {
          val insertFuture = tablesCollection.insert[Table](  // insert the new table to the vacant place
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

  def handleUpdateTable(msg: MessageUpdateTable): JsValue = {
    import adapters.ClientMessage._

    val updateFuture: Future[UpdateWriteResult] = // update table by id
      tablesCollection.update[JsObject, JsObject](Json.obj("id" -> msg.table.id),
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

  def handleRemoveTable(msg: MessageRemoveTable) = {
    val removeFuture = tablesCollection.remove[JsObject](Json.obj("id" -> msg.id)) // remove table by id

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
