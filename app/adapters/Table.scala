package adapters.messages

import play.api.libs.json.Json

object Table {
  implicit val format = Json.format[Table]
}

case class Table(id: Int, name: String, participants: Int)
