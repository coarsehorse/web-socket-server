package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageTableRemoved extends MessageObjectType("table_removed") {
  implicit val format: OFormat[MessageTableRemoved] = Json.format[MessageTableRemoved]
}

case class MessageTableRemoved ($type: String, id: Int)
  extends ClientMessage
