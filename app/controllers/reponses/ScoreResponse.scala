package controllers.reponses

import play.api.libs.json.{Json, OFormat}

case class ScoreResponse(score: Double)

object ScoreResponse {

  implicit val modelFormat: OFormat[ScoreResponse] = Json.format[ScoreResponse]
}