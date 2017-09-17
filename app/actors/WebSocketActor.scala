package actors

import javax.inject.Inject

import adapters._
import adapters.messages._
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import controllers.WebSocketController
import models.{ModelDAO, UsersCollection}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import play.modules.reactivemongo.json._

object WebSocketActor {
  private var userIsAuthenticated: Boolean = true
  println("userIsAuthenticated set to always be TRUE")
  private var userIsSubscribed: Boolean = false
  private var userIsAdmin: Boolean = true
  println("userIsAdmin set to always be TRUE")

  private var usersColl: ModelDAO = _
  private var tablesColl: ModelDAO = _

  def props(out: ActorRef, usersCollection: ModelDAO, tablesCollection: ModelDAO)
           (implicit ex: ExecutionContext): Props = {
    usersColl = usersCollection
    tablesColl = tablesCollection

    Props(new WebSocketActor(out))
  }
}

class WebSocketActor(out: ActorRef)
                    (implicit ex: ExecutionContext) extends Actor {
  import WebSocketActor._

  override def preStart(): Unit = {
    println("Started: " + this)
  }
  override def postStop() {
    println("Stopped: " + this)
  }

  def handleMessage(msg: ClientMessage): JsValue = {
    // debug
    println("in handleMessage(), my shit: " + msg)
    //
    msg match {
        // Available to everyone
    case msg: MessageLogin => handleMessageLogin(msg)

      // Available to authorised users
    case msg: ClientMessage if !userIsAuthenticated
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
    case msg: ClientMessage if !userIsAdmin
                                  => MessageError("not_authorized")
    case msg: MessageAddTable     => handleMessageAddTable(msg)
    case msg: MessageUpdateTable  => handleMessageUpdateTable(msg)
    case msg: MessageRemoveTable  => handleMessageRemoveTable(msg)
  }}

  def receive: PartialFunction[Any, Unit] = {
    case clientMessage: JsValue =>
      val response = handleMessage(clientMessage)
      out ! response
  }

  def handleMessagePing(msg: MessagePing): JsValue = {
    MessagePong(MessagePong.MSG_TYPE, msg.seq)
  }

  def handleMessageLogin(msg: MessageLogin): JsValue = {
    val findFuture = usersColl.findOne[User](Json.obj("username" -> msg.username))

    Await.result(findFuture, 3 seconds) match {
      case Some(u) =>                     // username from req is present in db
        if (u.password == msg.password) { // password is true
          userIsAuthenticated = true
          if (u.user_type == "admin")     // check user role
            userIsAdmin = true
          else
            userIsAdmin = false
          MessageLoginSuccessful(MessageLoginSuccessful.MSG_TYPE, u.user_type)
        }
        else {                            // password is false
          userIsAuthenticated = false
          MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
        }
      case None =>                      // username from req is NOT present in db
        userIsAuthenticated = false
        MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
    }
  }

  def handleMessageSubscribe(msg: MessageSubscribe): JsValue = {
    import SubscriptionActor._

    val subsActor = WebSocketController.getSubscriptionActor
    subsActor ! Subscribe(out)
    userIsSubscribed = true

    val findFuture = tablesColl.find[Table]()

    Await.result(findFuture, 3 seconds) match {
      case tables: List[Table] =>
        MessageTableList(MessageTableList.MSG_TYPE, tables)
      case _ =>
        MessageTableList(MessageTableList.MSG_TYPE, List())
    }
  }

  def handleMessageUnSubscribe(msg: MessageUnSubscribe): JsValue = {
    import SubscriptionActor._

    val subsActor = WebSocketController.getSubscriptionActor
    subsActor ! UnSubscribe(out)
    userIsSubscribed = false

    MessageUnSubscribeDone(MessageUnSubscribeDone.MSG_TYPE)
  }

  def handleMessageAddTable(msg: MessageAddTable): JsValue = {
    val updateFuture = tablesColl.update(
      selector = Json.obj("id" -> Json.obj("$gt" -> msg.after_id)),
      updater = Json.obj("$inc" -> Json.obj("id" -> 1)),
      multi = true
    )

    Await.result(updateFuture, 3 seconds) match {
      case updateRes: WriteResult =>
        if (updateRes.ok) {
          val insertFuture = tablesColl.insert[Table](
            Table(msg.after_id + 1, msg.table.name, msg.table.participants))

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

    val updateFuture = tablesColl.update[JsObject, JsObject](
      Json.obj("id" -> msg.table.id),
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
    val removeFuture = tablesColl.remove[JsObject](Json.obj("id" -> msg.id))

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
