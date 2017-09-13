package actors

import akka.actor._

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  override def postStop() {
    // cleanup
    println(this.toString + " is closed")
  }

  def receive = {
    case msg: String =>
      out ! ("Your message in UPPER CASE: " + msg.toUpperCase)
  }
}