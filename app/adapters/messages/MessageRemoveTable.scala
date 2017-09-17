package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageRemoveTable extends MessageObjectType("remove_table") {
  implicit val format = Json.format[MessageRemoveTable]
}

case class MessageRemoveTable ($type: String, id: Int)
  extends ClientMessage
