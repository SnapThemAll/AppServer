package utils.xml

import java.time.ZonedDateTime
import models.elevation.GoogleElevationRequest
import models.geometry.Point
import scala.xml.XML

/**
  * Helper object to parse some xml entries
  */
object XMLParser {

  /**
    * Parse a xml (formatted as gpx) representing one track or more
    * to retrieve the name, the date and the point from it
    *
    * @param track the xml String to parse
    * @return a list of 3-tuple containing the name, the date and the points
    */
  def nameDatePointsFromString(track: String): Seq[(String, ZonedDateTime, IndexedSeq[Point])] = {
    val allTracksAsXML = XML.loadString(track) \ "trk"

    allTracksAsXML.map { trackAsXML =>
      val pointsAsXML = trackAsXML \ "trkseg" \ "trkpt"

      val name = (trackAsXML \ "name").text

      val dateTimeOfFirstPoint     = ZonedDateTime.parse((pointsAsXML \ "time").head.text)
      val timeOfFirstPointInMillis = dateTimeOfFirstPoint.toInstant.toEpochMilli

      val points = pointsAsXML.map { node =>
        val timeOfPointInMillis = ZonedDateTime.parse((node \ "time").text).toInstant.toEpochMilli
        Point((node \ "@lat").text.toDouble,
              (node \ "@lon").text.toDouble,
              timeOfPointInMillis - timeOfFirstPointInMillis)
      }.toIndexedSeq

      val altitudeManager = new GoogleElevationRequest()

      for(p <- points){
        altitudeManager.addLocation(p.latitude, p.longitude)
      }

      val altitudes = altitudeManager.build().toIndexedSeq;

      require(altitudes.size == points.size, "The two list (altitudes and points) should have the same amount of points")

      val finalPoints: IndexedSeq[Point] = for(i <- (0 until altitudes.size)) yield {
        val p = points(i)
        Point(p.latitude, p.longitude, p.time, altitudes(i))
      }

      (name, dateTimeOfFirstPoint, finalPoints)
    }
  }
}
