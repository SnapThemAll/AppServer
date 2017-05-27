package models

import play.api.libs.json.{Json, OFormat}

case class Message(
                  serverIsUpdating: String,
                  latestNews: String,
                  _id: String = "default"
                  ) {}

object Message {
  implicit val jsonFormat: OFormat[Message] = Json.format[Message]

}