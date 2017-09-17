package adapters.messages

import play.api.libs.json.Json

object User {
  implicit val format = Json.format[User]
}

case class User(username: String, password: String, user_type: String)
