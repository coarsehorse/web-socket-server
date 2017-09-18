package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessagePong extends MessageObjectType("pong") {
  implicit val format: OFormat[MessagePong] = Json.format[MessagePong]
}

case class MessagePong($type: String, seq: Int) extends ClientMessage
