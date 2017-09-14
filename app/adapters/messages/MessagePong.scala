package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessagePong extends MessageObjectType("pong") {
  implicit val format = Json.format[MessagePong]
}

case class MessagePong($type: String, seq: Int) extends ClientMessage
