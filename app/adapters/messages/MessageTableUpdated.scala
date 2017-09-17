package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageTableUpdated extends MessageObjectType("table_updated") {
  implicit val format = Json.format[MessageTableUpdated]
}

case class MessageTableUpdated ($type: String, table: Table)
  extends ClientMessage
