package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageTableList extends MessageObjectType("table_list") {
  implicit val format = Json.format[MessageTableList]
}

case class MessageTableList($type: String, tables: List[Table]) extends ClientMessage
