package adapters

import play.api.libs.json.Json

object ErrorMessage extends MessageObjectType("message_error") {
  implicit val format = Json.format[ErrorMessage]
}

case class ErrorMessage($type: String) extends ClientMessage