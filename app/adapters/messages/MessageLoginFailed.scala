package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.Json

object MessageLoginFailed extends MessageObjectType("login_failed") {
  implicit val format = Json.format[MessageLoginFailed]
}

case class MessageLoginFailed($type: String) extends ClientMessage
