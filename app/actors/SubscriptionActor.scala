package actors

import adapters.ClientMessage
import akka.actor.{Actor, ActorRef, Props, Terminated}

object SubscriptionActor {
  def props: Props = Props(new SubscriptionActor())

  final case class Subscribe(socketActorRef: ActorRef)
  final case class UnSubscribe(socketActorRef: ActorRef)
  final case class Broadcast(message: ClientMessage)
}

/**
  * This actor is used for notifying subscribed clients
  */
class SubscriptionActor extends Actor {

  import SubscriptionActor._

  private var subscribers: Seq[ActorRef] = Seq.empty

  override def receive: PartialFunction[Any, Unit] = {
    // Subscribe for notifications
    case Subscribe(userActor) =>
      subscribers.find(_ == userActor) match {
        case None =>      // no duplicates
          subscribers = subscribers ++ Seq(userActor)
          context.watch(userActor)
        case Some(_) => ; // this actor is already subscribed
      }

    // Unsubscribe from notifications
    case UnSubscribe(userActor) =>
      subscribers.find(_ == userActor) match {
        case Some(_) => // actor is present
          subscribers = subscribers diff Seq(userActor)
        case None => ;  // there is no one to unsubscribe
      }

    // Notify subscribers
    case Broadcast(message) =>
      import adapters.ClientMessage._

      subscribers foreach (_ ! clientMessage2jsValue(message))

    // Subscriber is dead
    case Terminated(deceased) =>
      self ! UnSubscribe(deceased)
  }
}
