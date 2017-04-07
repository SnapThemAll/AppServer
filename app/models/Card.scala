package models

import java.util.UUID

import play.api.libs.json.Json

/**
  * The Card object.
  *
  * @param userID The unique ID of the user that owns the card.
  */
case class Card(cardName: String,
                userID: UUID,
                picturesURI: IndexedSeq[String],
                score: Double
               ) {
  require(score >= 0.0)
  require(score <= 10.0)

}

object Card {

  /**
    * An implicit writer to export a card to JSON. Import this in your scope to use Json.toJson on a card.
    */
  implicit val jsonFormat = Json.format[Card]
}
