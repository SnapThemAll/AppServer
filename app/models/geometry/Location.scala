package models.geometry

import play.api.libs.json.Json

/**
  * Stores the location of a Track
  */
case class Location private (`type`: String, coordinates: Seq[Double])

object Location {

  val `type`: String = "Point"

  /**
    * Extracts the location of a track and returns a Location object. The location is set to the location of the first
    * point of the track. It's of type "Point"
    * @param track The track from which to extract location
    * @return The Location object containing the location of this track as a point in space
    */
  def apply(track: Track): Location = {
    Location(`type`, Seq(track.points(0).latitude, track.points(0).longitude))
  }

  /**
    * Extracts the location of a track and returns a Location object. The location is set to the location of the first
    * point of the track. It's of type "Point"
    * @param point The point of the track from which to extract location
    * @return The Location object containing the location of this track as a point in space
    */
  def apply(point: Point): Location = {
    Location(`type`, Seq(point.latitude, point.longitude))
  }

  /**
    * Extracts the location of a track and returns a Location object. The location is set to the location of the first
    * point of the track. It's of type "Point"
    * @param longitude The longitude of the point
    * @param latitude The latitude of the point
    * @return The Location object containing the location of this track as a point in space
    */
  def apply(longitude: Double, latitude: Double): Location = {
    Location(`type`, Seq(longitude, latitude))
  }

  /**
    * An implicit writer to export a track to JSON. Import this in your scope to use Json.toJson on a track
    */
  implicit val implicitModelFormat = Json.format[Location]
}
