package computing

import models.Card

case class Category(name: String, pictures: Set[PictureFingerPrint]) {

}

object Category {
  def fromCard(card: Card): Category =
    Category(card.cardID, card.getNotDeleted.pictures.map(pic => PictureFingerPrint.fromPicture(pic)).toSet)
}
