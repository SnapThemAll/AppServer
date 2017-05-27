package models

import java.util.UUID

import play.api.libs.json.{Json, OFormat}
import utils.DataVariables

/**
  * The Card object.
  *
  * @param fbID The unique ID of the user that owns the card.
  */
case class Card(cardID: String,
                fbID: String,
                pictures: IndexedSeq[Picture] = IndexedSeq.empty,
                _id: UUID = UUID.randomUUID()
               ) {

  private def picturePath(fileName: String): String = DataVariables.pathToImage(fbID, cardID, fileName)

  def totalScore: Double = pictures.filterNot(_.deleted).map(_.score).sum

  def updatePicture(picture: Picture): Card =
    this.copy(
      pictures = pictures.filter(_.fileName != picture.fileName) :+ picture
    )

  def removePic(fileName: String): Card =
    this.copy(pictures = pictures.map{ picture =>
      if(picture.fileName == fileName) picture.delete
      else picture
    })

  def getNotDeleted: Card =
    this.copy(pictures = pictures.filterNot(_.deleted))

  def pictureFileNames: IndexedSeq[String] =
    pictures.map(pic => DataVariables.pathToImage(fbID, cardID, pic.fileName))

}
object Card {
  /**
    * An implicit writer to export a card to JSON. Import this in your scope to use Json.toJson on a card object.
    */
  implicit val jsonFormat: OFormat[Card] = Json.format[Card]
}

