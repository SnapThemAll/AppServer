package models

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json.{Json, OFormat}

case class Feedback(message: String, fbID: String, uuid: String = UUID.randomUUID().toString, date: ZonedDateTime = ZonedDateTime.now()){}

object Feedback {
  implicit val jsonFormat: OFormat[Feedback] = Json.format[Feedback]
}
