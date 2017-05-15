package computing

import models.{Card, PictureFingerPrint}

case class Category(name: String, pictures: Set[PictureFingerPrint]) {

  def addPictureFP(pictureFP: PictureFingerPrint): Category = this.copy(pictures = pictures + pictureFP)
}

object Category {
  def fromCard(card: Card): Category =
    Category(card.cardID, card.getNotDeleted.pictures.map(_.fingerPrint).toSet)
}
