package models

import play.api.libs.json.{Json, OFormat}

case class Update(serverVersion: String, serverIsUpdating: Boolean, uuid: String = "default") {}

object Update {
  implicit val jsonFormat: OFormat[Update] = Json.format[Update]

}