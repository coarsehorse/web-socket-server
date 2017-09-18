package controllers

import javax.inject.Inject

import actors.{SubscriptionActor, WebSocketActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import models.{TablesCollection, UsersCollection}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.Configuration
import play.api.Logger

import scala.concurrent.ExecutionContext

object WebSocketController {

  private var subscriptionActor: ActorRef = _

  def getSubscriptionActor: ActorRef = subscriptionActor
}

class WebSocketController @Inject()(cc: ControllerComponents, config: Configuration,
                                    usersCollection: UsersCollection, tablesCollection: TablesCollection)
                                   (implicit system: ActorSystem, mat: Materializer, exc: ExecutionContext)
  extends AbstractController(cc) {

    import WebSocketController._

    subscriptionActor = system.actorOf(SubscriptionActor.props, "subscriptionActor")

    Logger.info("Application URI: " + config.underlying.getString("app_route"))
    Logger.info("Database URI: " + config.underlying.getString("mongodb.uri"))

    /**
      * Flow of connections that is handled by actors
      */
    def socket: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
      ActorFlow.actorRef { client => // Flow that is handled by an actors
        WebSocketActor.props(client, usersCollection, tablesCollection) // Get actor handler props
      }                                                                 // and give access to the db
    }
}
