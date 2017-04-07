package jobs

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import jobs.TrackAnalysisManager.{Analyze, Done, Pending}
import models.analysis.{TrackAnalysisSet, TrackSpeed, TrackTime}
import models.daos.CardDAO
import models.geometry.{Location, Point}
import testutils.WithDAO

import scala.concurrent.duration._
import scala.util.{Random, Success, Try}
import play.api.libs.concurrent.InjectedActorSupport
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import org.scalatestplus.play.PlaySpec

class TrackAnalysisManagerTest extends PlaySpec with InjectedActorSupport with FutureAwaits with DefaultAwaitTimeout {
  implicit val timeout: Timeout = 20.seconds

  def getRandomDate =
    ZonedDateTime.parse("2016-11-28T17:00:00.001Z").plusNanos(1000 * 3600 * (Random.nextInt(100000) - 5000))
  def getPoints(date: ZonedDateTime) = IndexedSeq(
      Point(46.892934, 7.749051, Random.nextInt(10000)),
      Point(46.892935, 7.749051, Random.nextInt(10000)),
      Point(46.892936, 7.749051, Random.nextInt(10000)),
      Point(46.892937, 7.749051, Random.nextInt(10000)),
      Point(46.892938, 7.749051, Random.nextInt(10000)),
      Point(46.892939, 7.749051, Random.nextInt(10000)),
      Point(46.892940, 7.749051, Random.nextInt(10000)),
      Point(46.892941, 7.749051, Random.nextInt(10000)),
      Point(46.892942, 7.749051, Random.nextInt(10000)),
      Point(46.892943, 7.749051, Random.nextInt(10000)),
      Point(46.892944, 7.749051, Random.nextInt(10000)),
      Point(46.892945, 7.749051, Random.nextInt(10000))
  )

  val min      = 1
  val max      = 5
  val average  = 3
  val variance = 2
  val speeds   = IndexedSeq(1.9, 2.1, 3.1)

  val speedAnalysis                = TrackSpeed(min, max, average, variance, speeds)
  def timeAnalysis(duration: Long) = TrackTime(duration, 0, 2)

  def getTrack(userID: UUID = UUID.randomUUID(),
               trackID: UUID = UUID.randomUUID(),
               points: IndexedSeq[Point] = getPoints(getRandomDate)) =
    Track(UUID.randomUUID(),
          userID,
          "TrackName",
          getRandomDate,
          points,
          Location(getPoints(getRandomDate).head),
          TrackAnalysisSet(),
          None)

  def getAnalyzedTrack(track: Track): Track =
    track.addAnalysis(speedAnalysis).addAnalysis(timeAnalysis(track.points.last.time))

  "models.Track analysis manager" should {

    "preserve original track and set analysis field in response" in new WithDAO[CardDAO]("preserve_original") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")

      val track = getTrack()

      val Done(futureAnalyzedTrack) = await((analysisManager ? Analyze(track)).mapTo[Done])
      val analyzedTrack             = await(futureAnalyzedTrack)

      analyzedTrack.analyses must not be empty
      track.copy(analyses = analyzedTrack.analyses) mustBe analyzedTrack
    }
    "really be done when he tells so" in new WithDAO[CardDAO]("really_done") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")

      val track = getTrack()

      val Done(futureTrack) = await((analysisManager ? Analyze(track)).mapTo[Done])
      await(futureTrack)

      val response2 = await(analysisManager ? Analyze(track))
      response2 must not be a[Pending] //He shouldn't be already analysing this track (he answered done before)
    }
    "correctly set all the analyses" in new WithDAO[CardDAO]("correctly_set_all_analyses") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")

      val track = getTrack()

      val Done(futureAnalyzedTrack) = await((analysisManager ? Analyze(track)).mapTo[Done])
      val analyzedTrack             = await(futureAnalyzedTrack)

      analyzedTrack.analyses must have size 5
    }
    "tell pending if already analyzing a track" in new WithDAO[CardDAO]("tell_pending") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")

      val track = getTrack()

      analysisManager ! Analyze(track)
      val response2 = await((analysisManager ? Analyze(track)).mapTo[Pending])

      response2 mustBe Pending(track)
    }
    /* Now we can't anymore create tracks with no points. So this won't fail the same way
    "correctly handle failures" in new WithDAO[TrackDAO]("manager-handle-failures") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")

      val userID  = UUID.randomUUID()
      val trackID = UUID.randomUUID()

      val diabolicTrack = getTrack(userID = userID, trackID = trackID, points = IndexedSeq())
      val niceTrack     = getTrack(userID = userID, trackID = trackID)

      val Done(futureAnalyzedTrack) = await((analysisManager ? Analyze(diabolicTrack)).mapTo[Done])

      Try(await(futureAnalyzedTrack)) must not be a[Success[_]] //The analyse should have failed

      val retry = await((analysisManager ? Analyze(niceTrack)).mapTo[Done]) //We try again with a track with same ids
      retry must not be a[Pending] //Should have cancelled the job because of last failure => should accept this one
    }
    */
    "correctly insert analyzed track into the DAO" in new WithDAO[CardDAO]("correctly_insert_analyzed") {
      val analysisManager = injectNamed[ActorRef]("track-analysis-manager-actor")

      val track = getTrack()

      val Done(futureAnalyzedTrack) = await((analysisManager ? Analyze(track)).mapTo[Done])
      await(futureAnalyzedTrack)

      val optRetrieved = await(dao.find(userID = track.trackID))
      optRetrieved must not be empty

      val retrieved = optRetrieved.get
      retrieved.analyses must not be empty
      track.copy(analyses = retrieved.analyses) mustBe retrieved
    }
  }
}
