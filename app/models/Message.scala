package models

import java.util.UUID

import play.api.libs.json.{Json, OFormat}

case class Message(
                  serverIsUpdating: String,
                  appIsOutOfDate: String,
                  latestNews: String,
                  latestVersion: String,
                  _id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000") // IT'S UNIQUE ATM
                  ) {}

object Message {
  implicit val jsonFormat: OFormat[Message] = Json.format[Message]

}