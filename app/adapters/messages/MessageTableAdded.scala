package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageTableAdded extends MessageObjectType("table_added") {
  implicit val format = Json.format[MessageTableAdded]
}

case class MessageTableAdded($type: String, after_id: Int, table: Table) extends ClientMessage
