package models

import controllers.reponses.PictureDataResponse
import play.api.libs.json.{Json, OFormat}

/**
  * The Card object.
  *
  * @param fbID The unique ID of the user that owns the card.
  */
case class Card(cardName: String,
                fbID: String,
                pictures: IndexedSeq[Picture] = IndexedSeq.empty
               ) {

  def bestScore: Double = pictures.map(_.score).max
  def addPic(fileName: String, score: Double): Card =
    this.copy(pictures = pictures.filter(_.fileName != fileName) :+ Picture(fileName, score))
  def removePic(fileName: String): Card =
    this.copy(pictures = pictures.map{ picture =>
      if(picture.fileName == fileName) picture.delete
      else picture
    })

  def getNotDeleted: Card =
    this.copy(pictures = pictures.filterNot(_.deleted))
}
object Card {

  /**
    * An implicit writer to export a card to JSON. Import this in your scope to use Json.toJson on a card object.
    */
  implicit val jsonFormat: OFormat[Card] = Json.format[Card]
}

