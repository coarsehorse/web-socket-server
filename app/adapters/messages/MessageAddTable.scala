package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object TableWithoutId {
  implicit val format: OFormat[TableWithoutId] = Json.format[TableWithoutId]
}

case class TableWithoutId(name: String, participants: Int)

object MessageAddTable extends MessageObjectType("add_table") {
  implicit val format: OFormat[MessageAddTable] = Json.format[MessageAddTable]
}

case class MessageAddTable ($type: String, after_id: Int, table: TableWithoutId)
  extends ClientMessage
