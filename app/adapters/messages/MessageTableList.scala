package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageTableList extends MessageObjectType("table_list") {
  implicit val format: OFormat[MessageTableList] = Json.format[MessageTableList]
}

case class MessageTableList($type: String, tables: List[Table]) extends ClientMessage
