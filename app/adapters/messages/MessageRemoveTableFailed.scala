package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageRemoveTableFailed extends MessageObjectType("removal_failed") {
  implicit val format = Json.format[MessageRemoveTableFailed]
}

case class MessageRemoveTableFailed ($type: String, id: Int)
  extends ClientMessage
