package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer

object SubscriptionActor {
  def props: Props = Props(new SubscriptionActor())

  final case class Subscribe(socketActorRef: ActorRef)
  final case class UnSubscribe(socketActorRef: ActorRef)
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
  }
}
