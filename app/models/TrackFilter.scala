package models

import java.util.UUID

import play.api.libs.json.Json

/**
  * Parameters for the prediction requests
  *
  * @param userID The user willing to have the predicted tracks
  * @param amount The amount of desired predictions (the maximum number of predictions to return, not minimum)
  * @param nearLat The latitude of the place near which we would like to have the predictions
  * @param nearLng The longitude of the place near which we would like to have the predictions
  * @param minLength The minimum length (in milliseconds) of the tracks used for the predictions (default no minimum)
  * @param maxLength The maximum length (in milliseconds) of the tracks used for the predictions (default no maximum)
  */
case class TrackFilter(userID: UUID,
                       amount: Int = 5,
                       nearLat: Double = 46.892934,
                       nearLng: Double = 7.749051,
                       minLength: Long = 0,
                       maxLength: Long = Long.MaxValue)

object TrackFilter {
  /*
  /**
   * Parameters for the prediction requests
   *
   * @param userID The user willing to have the predicted tracks
   * @param amount The amount of desired predictions (the maximum number of predictions to return, not minimum)
   * @param near The place near which we would like to have the predictions, given as longitude and latitude
   * @param after Get only predictions based on tracks after the specified date. The following date must be parsable
   *              using ZonedDateTime.parse
   * @param minLength The minimum length (in milliseconds) of the tracks used for the predictions (default no minimum)
   * @param maxLength The maximum length (in milliseconds) of the tracks used for the predictions (default no maximum)
   */
  def apply(userID: UUID,
            amount: Int = 5,
            near: Seq[Double] = Seq(),
            after: String,
            minLength: Long = 0,
            maxLength: Long = Long.MaxValue): TrackFilter =
    TrackFilter(userID, amount, near, ZonedDateTime.parse(after), minLength, maxLength)
   */

  /**
    * An implicit writer to export a user to JSON. Import this in your scope to use Json.toJson on a user
    */
  implicit val trackFilterJsonFormat = Json.format[TrackFilter]
}
