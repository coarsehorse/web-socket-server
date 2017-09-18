package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessagePing extends MessageObjectType("ping") {
  implicit val format: OFormat[MessagePing] = Json.format[MessagePing]
}

case class MessagePing($type: String, seq: Int) extends ClientMessage
