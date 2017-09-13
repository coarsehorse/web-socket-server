package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject

import actors.WebSocketActor
import akka.actor.ActorSystem
import akka.stream.Materializer

class WebSocketController @Inject()(cc:ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def socket = WebSocket.accept[String, String] { request =>
    println("user: " + request.session.get("user"))
    ActorFlow.actorRef { out => // Flow that is handled by an actor from 'out' ref
      WebSocketActor.props(out) // Create an actor for new connected WebSocket
    }
  }

}

object WebSocketController {
  implicit val
}
