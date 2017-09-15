package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageSubscribe extends MessageObjectType("subscribe_tables") {
  implicit val format = Json.format[MessageSubscribe]
}

case class MessageSubscribe($type: String) extends ClientMessage
