package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageUpdateTable extends MessageObjectType("update_table") {
  implicit val format = Json.format[MessageUpdateTable]
}

case class MessageUpdateTable ($type: String, table: Table)
  extends ClientMessage
