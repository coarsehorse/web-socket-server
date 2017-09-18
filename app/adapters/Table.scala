package adapters.messages

import play.api.libs.json.{Json, OFormat}

object Table {
  implicit val format: OFormat[Table] = Json.format[Table]
}

case class Table(id: Int, name: String, participants: Int)
