package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageUpdateTableFailed extends MessageObjectType("update_table_failed") {
  implicit val format = Json.format[MessageUpdateTableFailed]
}

case class MessageUpdateTableFailed ($type: String)
  extends ClientMessage