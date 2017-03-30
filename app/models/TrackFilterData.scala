package models

import java.util.UUID

import play.api.libs.json.Json

/**
  * Parameters for the prediction requests as received by the requester. Its purpose it to then be converted to
  * [[models.TrackFilter]]
  *
  * @param amount The amount of desired predictions (the maximum number of predictions to return, not minimum)
  * @param nearLat The latitude of the place near which we would like to have the predictions
  * @param nearLng The longitude of the place near which we would like to have the predictions
  * @param minLength The minimum length (in milliseconds) of the tracks used for the predictions (default no minimum)
  * @param maxLength The maximum length (in milliseconds) of the tracks used for the predictions (default no maximum)
  */
case class TrackFilterData(amount: Option[Int] = None,
                           nearLat: Option[Double] = None,
                           nearLng: Option[Double] = None,
                           minLength: Option[Long] = None,
                           maxLength: Option[Long] = None) {

  def toTrackFilter(userID: UUID): TrackFilter =
    TrackFilter(
        userID,
        amount.getOrElse(5),
        nearLat.getOrElse(46.892934),
        nearLng.getOrElse(7.749051),
        minLength.getOrElse(0l),
        maxLength.getOrElse(Long.MaxValue)
    )
}

object TrackFilterData {

  /**
    * An implicit writer to export a user to JSON. Import this in your scope to use Json.toJson on a user
    */
  implicit val trackFilterDataJsonFormat = Json.format[TrackFilterData]
}
