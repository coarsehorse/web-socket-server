package adapters.messages

import play.api.libs.json.{Json, OFormat}

object User {
  implicit val format: OFormat[User] = Json.format[User]
}

case class User(username: String, password: String, user_type: String)
