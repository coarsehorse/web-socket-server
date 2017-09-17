package actors

import adapters.ClientMessage
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}

object SubscriptionActor {
  def props: Props = Props(new SubscriptionActor())

  final case class Subscribe(socketActorRef: ActorRef)
  final case class UnSubscribe(socketActorRef: ActorRef)
  final case class Broadcast(message: ClientMessage)
}

class SubscriptionActor extends Actor {
  import SubscriptionActor._

  private var subscribers = Seq.empty[ActorRef]

  override def receive = {
    case Subscribe(userActor) =>
      println("subscribers before ++: " + subscribers)
      subscribers.find(_ == userActor) match {
        case None =>
          subscribers = subscribers ++ Seq(userActor)
          context.watch(userActor)
        case Some(_) => ;
      }
      println("subscribers after ++: " + subscribers)

    case UnSubscribe(userActor) =>
      println("subscribers before --: " + subscribers)
      subscribers.find(_ == userActor) match {
        case Some(_) =>
          subscribers = subscribers diff Seq(userActor)
        case None => ;
      }
      println("subscribers after --: " + subscribers)

    case Broadcast(message) =>
      import adapters.ClientMessage._

      println("Before broadcast")
      subscribers foreach (_ ! clientMessage2jsValue(message))
      println("Broadcasted")

    case Terminated(deceased) =>
      self ! UnSubscribe(deceased)
  }
}
