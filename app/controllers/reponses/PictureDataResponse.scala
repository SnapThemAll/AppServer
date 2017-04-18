package controllers.reponses

import models.{Card, Picture}
import play.api.libs.json.{Json, OFormat}


case class PictureDataResponse(cardName: String, fileName: String, score: Double)

object PictureDataResponse{

  def fromCard(card: Card): IndexedSeq[PictureDataResponse] =
    fromPictures(card.cardName, card.pictures)

  def fromPictures(cardName: String, pictures: IndexedSeq[Picture]): IndexedSeq[PictureDataResponse] =
    pictures.filterNot(_.deleted).map{ pic =>
      PictureDataResponse(
        cardName,
        pic.fileName,
        pic.score
      )}

  implicit val modelFormat: OFormat[PictureDataResponse] = Json.format[PictureDataResponse]
}