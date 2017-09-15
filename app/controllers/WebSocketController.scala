package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject

import actors.{SubscriptionActor, WebSocketActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, Materializer}
import play.api.libs.json.JsValue

object WebSocketController {
  private var subscriptionActor: ActorRef = _

  def getSubscriptionActor: ActorRef = subscriptionActor
}

class WebSocketController @Inject()(cc:ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {
    import WebSocketController._
    subscriptionActor = system.actorOf(SubscriptionActor.props, "subscriptionActor")

    def socket: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
      ActorFlow.actorRef { out => // Flow that is handled by an actor from 'out' ref
        WebSocketActor.props(out) // Create an actor for new connected WebSocket
      }
    }
}