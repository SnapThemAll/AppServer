import java.time.ZonedDateTime
import java.util.UUID

import models.TrackData
import models.analysis.{TrackAnalysis, TrackAnalysisSet, TrackAnalyzer}
import models.geometry.{Point, Track}
import models.profile.UserProfile

import scala.util.Random

import play.api.libs.json.Json


def date = ZonedDateTime.parse("2015-11-28T17:00:00.001Z").plusHours(Random.nextInt(24 * 365))
def points(n: Int) = {
  Vector.tabulate(n) { i =>
    Point(46.892934 + i * 0.00001, 7.749051 + i * 0.00001, 1000 * i + Random.nextInt(100) - 50, 0)
  }
}

def createTrack(n: Int) =
  Track(UUID.randomUUID(),
    UUID.randomUUID(),
    "TrackName" + Random.nextInt(10000),
    date,
    points(n),
    TrackAnalysisSet(),
    None)

def createTrackData(n: Int): TrackData = createTrack(n).toTrackData

val tracks = Vector.tabulate(20)(_ => createTrack(300 + Random.nextInt(1000))).map{ track =>
  val analyzer = TrackAnalyzer(track)
  track.addAnalyses(
    TrackAnalysisSet(
      TrackAnalysis.allTrackObjects.map{ _.apply(analyzer)})
  )
}

val profile = UserProfile(tracks)

tracks.map( track => Json.toJson(track.toTrackData) ).mkString("\n")