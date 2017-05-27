package models

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json.{Json, OFormat}

case class Feedback(
                     message: String,
                     fbID: String,
                     date: ZonedDateTime = ZonedDateTime.now(),
                     _id: UUID = UUID.randomUUID()
                   ){}

object Feedback {
  implicit val jsonFormat: OFormat[Feedback] = Json.format[Feedback]
}
