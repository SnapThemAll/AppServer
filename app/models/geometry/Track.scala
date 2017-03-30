package models.geometry

import java.time.ZonedDateTime
import java.util.UUID

import models.TrackData
import models.analysis.{TrackAnalysis, TrackAnalysisSet, TrackAnalyzer, TrackDistance}
import shapeless.Typeable
import utils.xml.XMLWriter

import play.api.libs.json.Json

/**
  * The track object.
  *
  * @param trackID The unique ID of the track.
  * @param userID The unique ID of the user that own the track.
  * @param date date in ms of the start of the recording
  * @param points list of points of the track in correct order
  * @param loc The location of this track. Generally the location of its first point
  * @param analyses An option on a TrackAnalysis
  * @param tags Maybe the tags associated to the track.
  */
case class Track(trackID: UUID,
                 userID: UUID,
                 name: String,
                 date: ZonedDateTime,
                 points: IndexedSeq[Point],
                 loc: Location,
                 analyses: TrackAnalysisSet,
                 tags: Option[Seq[String]]) {
  require(points.length > 2)

  def toTrackData: TrackData =
    TrackData(
        trackID,
        XMLWriter.trackToXML(this).toString,
        tags
    )

  /**
    * Adds an analysis to the current analyses of this track
    *
    * @param analysis The new analysis computed for this track
    * @return This track to which is added the analysis
    */
  def addAnalysis(analysis: TrackAnalysis): Track =
    copy(analyses = analyses + analysis)

  def addAnalyses(analysisSet: TrackAnalysisSet) = {
    copy(analyses = analyses ++ analysisSet)
  }

  def getAnalysis[T <: TrackAnalysis: Typeable]: Option[T] = analyses.get[T]

}

object Track {

  /**
    * The track object. The location will be automatically extracted from the sequence of points
    *
    * @param trackID The unique ID of the track.
    * @param userID The unique ID of the user that own the track.
    * @param date date in ms of the start of the recording
    * @param points list of points of the track in correct order
    * @param analyses An option on a TrackAnalysis
    * @param tags Maybe the tags associated to the track.
    */
  def apply(trackID: UUID,
            userID: UUID,
            name: String,
            date: ZonedDateTime,
            points: IndexedSeq[Point],
            analyses: TrackAnalysisSet,
            tags: Option[Seq[String]]): Track = {
    require(points.length > 2, "A track must have at least 2 points")
    Track(trackID, userID, name, date, points, Location(points.head), analyses, tags)
  }

  /**
    * An implicit writer to export a track to JSON. Import this in your scope to use Json.toJson on a track
    */
  implicit val implicitModelFormat = Json.format[Track]
}
