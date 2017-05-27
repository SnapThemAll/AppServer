package models

import play.api.libs.json.{Json, OFormat}

case class Message(
                  serverIsUpdating: String,
                  appIsOutOfDate: String,
                  latestNews: String,
                  latestVersion: String,
                  _id: String = "default"
                  ) {}

object Message {
  implicit val jsonFormat: OFormat[Message] = Json.format[Message]

}