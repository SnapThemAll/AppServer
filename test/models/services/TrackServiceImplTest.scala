package models.services

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import jobs.TrackAnalysisManager.{Analyze, Pending}
import models.TrackData
import models.daos.TrackDAO
import testutils.WithDAO

import scala.concurrent.duration._

import play.api.libs.concurrent.InjectedActorSupport
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import org.scalatestplus.play.PlaySpec

class TrackServiceImplTest extends PlaySpec with InjectedActorSupport with FutureAwaits with DefaultAwaitTimeout {
  implicit val timeout: Timeout = 5.minute

  val pointsAsXML =
    <gpx>
      <trk>
        <name>TrackName</name>
        <trkseg>
          <trkpt lat="46.858281" lon="7.842435">
            <time>2016-11-28T17:00:00.001Z</time>
          </trkpt>
          <trkpt lat="46.858282" lon="7.842435">
            <time>2016-11-28T17:00:02.050Z</time>
          </trkpt>
          <trkpt lat="46.858283" lon="7.842435">
            <time>2016-11-28T17:00:03.964Z</time>
          </trkpt>
        </trkseg>
      </trk>
    </gpx>

  val userID  = UUID.randomUUID()
  val trackID = UUID.randomUUID()

  val trackData = TrackData(trackID, pointsAsXML.toString(), None)
  val track     = trackData.toTrack(userID)

  "models.Track service" should {

    "analyze a newly inserted track" in new WithDAO[TrackDAO]("preserve_original") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")
      val trackService    = inject[TrackService]

      await(trackService.save(userID, trackData))

      //How to check if analysis has been triggered by the track service ? Check if analysis manager is analysing it
      val response = await((analysisManager ? Analyze(track)).mapTo[Pending])

      response mustBe Pending(track)
    }
  }

}
