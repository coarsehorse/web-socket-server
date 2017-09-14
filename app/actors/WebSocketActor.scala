package actors

import javax.inject.Inject

import adapters._
import adapters.messages._
import akka.actor.{Actor, ActorRef, Props}
import play.api.Configuration
import play.api.libs.json._

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  var isAuthenticated = false

  def handleMessage(msg: ClientMessage): JsValue = msg match {
    case msg: MessageLogin => handleMessageLogin(msg)
    case msg: ClientMessage if !isAuthenticated => ErrorMessage("not_authorized")
    case msg: MessagePing => handleMessagePing(msg)
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
    context.parent ! 117
    val presentInDb = true
    val user_type = "user"

    if (presentInDb) {
      isAuthenticated = true
      MessageLoginSuccessful(MessageLoginSuccessful.MSG_TYPE, user_type)
    }
    else
      MessageLoginFailed(MessageLoginFailed.MSG_TYPE)
  }
}
