package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageTableUpdated extends MessageObjectType("table_updated") {
  implicit val format: OFormat[MessageTableUpdated] = Json.format[MessageTableUpdated]
}

case class MessageTableUpdated ($type: String, table: Table)
  extends ClientMessage
