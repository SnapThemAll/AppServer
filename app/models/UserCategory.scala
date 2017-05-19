package models

case class UserCategory(name: String, picturesFP: Set[PictureFingerPrint]) extends Category {

  //def addPictureFP(pictureFP: PictureFingerPrint): UserCategory = this.copy(pictures = pictures + pictureFP)
}

object UserCategory {
  def fromCard(card: Card): UserCategory =
    UserCategory(card.cardID, card.pictures.map(_.fingerPrint).toSet)
  def fromCards(cards: Seq[Card]): UserCategory = {
    var cardID = cards.head.cardID
    val pictures = cards.map{ card =>
      require(card.cardID == cardID, "Can't build categories if cards have different cardID")
      card.pictures.map(_.fingerPrint).toSet
    }.reduce(_ ++ _)
    UserCategory(cardID, pictures)

  }
}
