package models.profile

import models.analysis._
import models.geometry.{TimeUtils, Track}

import scala.xml.Node
import scala.xml.Utility.trim

/**
  * Represent the profile of a user computed with the tracks he ran.
  *
  * @param totTracks number of tracks run by the user
  * @param longestTrack longest track of the user in km
  * @param totDist total distance run by the user in km
  * @param totTime total time spent running by the user in ms
  * @param totCalories total calories burnt by the user in kCal
  * @param totAltDif total altitude difference run by the user in m
  * @param averageSpeed average speed while running of the user in km/h
  */
case class UserProfile(
    totTracks: Long,
    longestTrack: Double,
    totDist: Double,
    totTime: Long,
    totCalories: Double,
    totAltDif: Double,
    averageSpeed: Double
) {
  def toXML: Node =
    trim(
        <profile>
          <nbTrack>{totTracks}</nbTrack>
          <lgstTrack>{longestTrack}</lgstTrack>
          <totDist>{totDist}</totDist>
          <totTime>{totTime}</totTime>
          <totCal>{totCalories}</totCal>
          <totAltDif>{totAltDif}</totAltDif>
          <avrgSpeed>{averageSpeed}</avrgSpeed>
        </profile>
    )

}

object UserProfile {

  def apply(tracks: Seq[Track]): UserProfile = fromAnalyses(tracks.map(_.analyses))

  def fromAnalyses(analyses: Seq[TrackAnalysisSet]): UserProfile = {
    val tracksDistance = analyses.flatMap(_.get[TrackDistance]).map(_.totDistance)
    val totDistance    = tracksDistance.sum
    val totTime        = analyses.flatMap(_.get[TrackTime]).map(_.trackDuration).sum

    UserProfile(
        analyses.length,
        tracksDistance.reduce(Math.max(_, _)),
        totDistance,
        totTime,
        analyses.flatMap(_.get[TrackEnergy]).map(_.caloriesBurnt).sum,
        analyses
          .flatMap(_.get[TrackAltitude])
          .map(trackAlt => trackAlt.positiveAltDiff + trackAlt.negativeAltDif)
          .sum,
        totDistance / TimeUtils.millisToHours(totTime)
    )
  }
}
