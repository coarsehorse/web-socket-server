package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageLogin extends MessageObjectType("login") {
  implicit val format: OFormat[MessageLogin] = Json.format[MessageLogin]
}

case class MessageLogin($type: String, username: String, password: String) extends ClientMessage
