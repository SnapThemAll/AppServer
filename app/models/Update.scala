package models

import java.util.UUID

import play.api.libs.json.{Json, OFormat}

case class Update(
                   serverVersion: String,
                   serverIsUpdating: Boolean,
                   _id:  UUID = UUID.fromString("00000000-0000-0000-0000-000000000000") // IT'S UNIQUE ATM
                 ) {}

object Update {
  implicit val jsonFormat: OFormat[Update] = Json.format[Update]

}