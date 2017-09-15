package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageUnSubscribeDone extends MessageObjectType("unsubscribe_tables_done") {
  implicit val format = Json.format[MessageUnSubscribeDone]
}

case class MessageUnSubscribeDone($type: String) extends ClientMessage
