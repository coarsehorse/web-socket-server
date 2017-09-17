package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageError extends MessageObjectType("message_error") {
  implicit val format = Json.format[MessageError]
}

/**
  * Universal error message
  * @param $type Error text(type)
  */
case class MessageError($type: String) extends ClientMessage