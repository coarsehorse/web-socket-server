package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageAddTableFailed extends MessageObjectType("add_table_failed") {
  implicit val format: OFormat[MessageAddTableFailed] = Json.format[MessageAddTableFailed]
}

case class MessageAddTableFailed ($type: String, after_id: Int)
  extends ClientMessage
