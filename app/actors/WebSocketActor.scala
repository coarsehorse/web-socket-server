package actors

import adapters._
import adapters.messages._
import akka.actor.{Actor, ActorRef, Props}
import controllers.WebSocketController
import models.Users
import play.api.libs.json._

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))

  private var isAuthenticated = true
  private var isSubscribed = false
}


class WebSocketActor(out: ActorRef) extends Actor {
  import WebSocketActor._

  override def preStart(): Unit = {
    println("Started: " + this)
  }
  override def postStop() {
    println("Stopped " + this)
  }

  def handleMessage(msg: ClientMessage): JsValue = msg match {
    case msg: MessageLogin => handleMessageLogin(msg)
    case msg: ClientMessage if !isAuthenticated => ErrorMessage("not_authorized")
    case msg: MessagePing => handleMessagePing(msg)
    case msg: MessageSubscribe => handleMessageSubscribe(msg)
    case msg: MessageUnSubscribe => handleMessageUnSubscribe(msg)

    case msg: ErrorMessage => msg
  }

  def receive: PartialFunction[Any, Unit] = {
    case request: JsValue =>
      val response = handleMessage(request)
      out ! response
  }

  def handleMessagePing(msg: MessagePing): JsValue = {
    MessagePong(MessagePong.MSG_TYPE, msg.seq)
  }

  def handleMessageLogin(msg: MessageLogin): JsValue = {
    // TODO: check login/pass in DB
    val presentInDb = true
    val user_type = "user"

    if (presentInDb) {
      isAuthenticated = true
      MessageLoginSuccessful(MessageLoginSuccessful.MSG_TYPE, user_type)
    }
    else
      MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
  }

  def handleMessageSubscribe(msg: MessageSubscribe): JsValue = {
    import SubscriptionActor._

    val subsActor = WebSocketController.getSubscriptionActor
    subsActor ! Subscribe(out)
    isSubscribed = true
    // TODO: make it normal
    MessageTableList(MessageTableList.MSG_TYPE,
      List(Table(1, "1", 1), Table(2, "2", 2)))
  }

  def handleMessageUnSubscribe(msg: MessageUnSubscribe): JsValue = {
    import SubscriptionActor._

    val subsActor = WebSocketController.getSubscriptionActor
    subsActor ! UnSubscribe(out)
    isSubscribed = false

    MessageUnSubscribeDone(MessageUnSubscribeDone.MSG_TYPE)
  }
}
