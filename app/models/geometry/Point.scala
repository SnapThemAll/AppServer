package models.geometry

import play.api.libs.json.Json

/**
  * Creates a point in geographic coordinates with an associated time in hours
  *
  * @param latitude
  *          Latitude of point
  * @param longitude
  *          Longitude of point
  * @param time
  *          the time in milliseconds when the position has been recorded relative to start of recording
  * @param elevation
  *          Elevation of the point in km
  *
  */
case class Point(latitude: Double, longitude: Double, time: Long, elevation: Double)

object Point {
  /**
    * Creates a point in geographic coordinates with an associated time in hours
    *
    * @param latitude
    *          Latitude of point
    * @param longitude
    *          Longitude of point
    * @param time
    *          the time in hours when the position has been recorded relative to start of recording
    *
    */
  def apply(latitude: Double, longitude: Double, time: Long): Point =
    Point(latitude, longitude, time, 0d)
  /**
    * An implicit writer to export a track to JSON. Import this in your scope to use Json.toJson on a track
    */
  implicit val implicitModelFormat = Json.format[Point]
}
