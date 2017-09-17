package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject

import actors.{SubscriptionActor, WebSocketActor}
import adapters.messages.{MessageLogin, User}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import models.{TablesCollection, UsersCollection}
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object WebSocketController {
  private var subscriptionActor: ActorRef = _

  def getSubscriptionActor: ActorRef = subscriptionActor
}

class WebSocketController @Inject()(cc: ControllerComponents,
                                    usersCollection: UsersCollection, tablesCollection: TablesCollection)
                                   (implicit system: ActorSystem, mat: Materializer, exc: ExecutionContext)
  extends AbstractController(cc) {
    import WebSocketController._

    subscriptionActor = system.actorOf(SubscriptionActor.props, "subscriptionActor")

    def socket: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
      ActorFlow.actorRef { out => // Flow that is handled by an actors
        WebSocketActor.props(out, usersCollection, tablesCollection)  // Get actor handler props
      }                                                               // and endow it db access
    }
}
