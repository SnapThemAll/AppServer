package controllers.reponses

import models.{Card, Picture}
import play.api.libs.json.{Json, OFormat}


case class PictureDataResponse(cardID: String, fileName: String, score: Double)

object PictureDataResponse{

  def fromCard(card: Card): IndexedSeq[PictureDataResponse] =
    fromPictures(card.cardID, card.pictures)

  def fromPictures(cardID: String, pictures: IndexedSeq[Picture]): IndexedSeq[PictureDataResponse] =
    pictures.filterNot(_.deleted).map{ pic =>
      PictureDataResponse(
        cardID,
        pic.fileName,
        pic.score
      )}

  implicit val modelFormat: OFormat[PictureDataResponse] = Json.format[PictureDataResponse]
}