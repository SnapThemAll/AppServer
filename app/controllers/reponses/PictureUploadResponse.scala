package controllers.reponses

import play.api.libs.json.{Json, OFormat}

import scala.util.Random


case class PictureUploadResponse(score: Double) {}

object PictureUploadResponse {
  def randomScore: PictureUploadResponse = {
    PictureUploadResponse(Random.nextDouble()*10)
  }

  implicit val modelFormat: OFormat[PictureUploadResponse] = Json.format[PictureUploadResponse]
}
