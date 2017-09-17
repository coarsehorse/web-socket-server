package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageTableRemoved extends MessageObjectType("table_removed") {
  implicit val format = Json.format[MessageTableRemoved]
}

case class MessageTableRemoved ($type: String, id: Int)
  extends ClientMessage
