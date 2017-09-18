package adapters.messages

import adapters.{ClientMessage, MessageObjectType}
import play.api.libs.json.{Json, OFormat}

object MessageLoginSuccessful extends MessageObjectType("login_successful") {
  implicit val format: OFormat[MessageLoginSuccessful] = Json.format[MessageLoginSuccessful]
}

case class MessageLoginSuccessful($type: String, user_type: String) extends ClientMessage
