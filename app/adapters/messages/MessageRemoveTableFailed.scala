package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageRemoveTableFailed extends MessageObjectType("removal_failed") {
  implicit val format: OFormat[MessageRemoveTableFailed] = Json.format[MessageRemoveTableFailed]
}

case class MessageRemoveTableFailed ($type: String, id: Int)
  extends ClientMessage
