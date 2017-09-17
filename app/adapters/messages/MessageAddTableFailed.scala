package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageAddTableFailed extends MessageObjectType("add_table_failed") {
  implicit val format = Json.format[MessageAddTableFailed]
}

case class MessageAddTableFailed ($type: String, after_id: Int)
  extends ClientMessage