package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageUnSubscribe extends MessageObjectType("unsubscribe_tables") {
  implicit val format = Json.format[MessageUnSubscribe]
}

case class MessageUnSubscribe($type: String) extends ClientMessage