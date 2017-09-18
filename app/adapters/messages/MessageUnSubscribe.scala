package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageUnSubscribe extends MessageObjectType("unsubscribe_tables") {
  implicit val format: OFormat[MessageUnSubscribe] = Json.format[MessageUnSubscribe]
}

case class MessageUnSubscribe($type: String) extends ClientMessage
