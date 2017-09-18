package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageSubscribe extends MessageObjectType("subscribe_tables") {
  implicit val format: OFormat[MessageSubscribe] = Json.format[MessageSubscribe]
}

case class MessageSubscribe($type: String) extends ClientMessage
