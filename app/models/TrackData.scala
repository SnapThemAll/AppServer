package models

import java.util.UUID

import models.analysis.TrackAnalysisSet
import models.geometry.Track
import utils.xml.XMLParser.nameDatePointsFromString

import play.api.libs.json.Json

/**
  * The track data object representation.
  *
  * @param trackID The unique ID of the track.
  * @param pointsAsXML The list of points formatted as XML (latitude, longitude, time, average speed and altitude).
  * @param tags Maybe the tags associated to the track.
  */
case class TrackData(trackID: UUID, pointsAsXML: String, tags: Option[Seq[String]]) {

  def toTrack(userID: UUID) = {
    val (name, date, points) = nameDatePointsFromString(pointsAsXML).head
    Track(
      trackID,
      userID,
      name,
      date,
      points,
      TrackAnalysisSet(),
      tags
    )
  }
}

object TrackData {

  /**
    * An implicit writer to export a track to JSON. Import this in your scope to use Json.toJson on a track
    */
  implicit val implicitModelFormat = Json.format[TrackData]
}
