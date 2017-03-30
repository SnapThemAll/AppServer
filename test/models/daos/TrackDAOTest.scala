package models.daos

import java.time.ZonedDateTime
import java.util.UUID

import models.TrackFilter
import models.analysis.{TrackAnalysisSet, TrackSpeed}
import models.geometry.{Location, Point}
import testutils.WithDAO

import scala.util.Random
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class TrackDAOTest extends PlaySpec with FutureAwaits with DefaultAwaitTimeout {

  def getRandomDate =
    ZonedDateTime.parse("2016-11-28T17:00:00.001Z").plusNanos(1000 * 3600 * (Random.nextInt(100000) - 5000))

  def getPoints(date: ZonedDateTime) = IndexedSeq(
      Point(46.892934, 7.749051, Random.nextInt(10000)),
      Point(46.892935, 7.749051, Random.nextInt(10000)),
      Point(46.892936, 7.749051, Random.nextInt(10000))
  )
  val min      = 1
  val max      = 5
  val average  = 3
  val variance = 2
  val speeds   = IndexedSeq(1.9, 2.1, 3.1)

  def getAnalysis = TrackSpeed(min, max, average, variance, speeds)

  def getTrack(userID: UUID = UUID.randomUUID()) = {
    val date     = getRandomDate
    val points   = getPoints(date)
    val analysis = getAnalysis
    Track(UUID.randomUUID(), userID, "TrackName", getRandomDate, points, TrackAnalysisSet(analysis), None)
  }

  "A TrackDAO" should {
    "save a track" in new WithDAO[TrackDAO]("save_track") {
      val track      = getTrack()
      val trackAdded = await(dao.save(track))
      trackAdded shouldBe track
    }
    "retrieves added track using trackID" in new WithDAO[TrackDAO]("save_and_retrieve_track") {
      val track = getTrack()
      await(dao.save(track))

      val trackFound = await(dao.find(track.trackID))
      trackFound shouldBe defined
      trackFound.get shouldBe track
    }
    "retrieve shared tracks of others" in new WithDAO[TrackDAO]("retrieve_shared_tracks_of_others") {
      val userID     = UUID.randomUUID()
      val userTracks = Vector.fill(10)(getTrack(userID))

      val othersQuantity = 5
      val othersTracks   = Vector.fill(othersQuantity)(getTrack())

      val allTracks = Random.shuffle(userTracks ++ othersTracks)
      allTracks.map(dao.save).map(await(_))

      val othersTracksRetrieved = await(dao.findShared(userID, othersQuantity))

      othersTracksRetrieved should have length othersQuantity
      othersTracksRetrieved foreach (_.userID should not be userID)
    }
    "retrieve shared tracks based on filter" in new WithDAO[TrackDAO]("retrieve_shared_tracks_filter") {
      val userID     = UUID.randomUUID()
      val userTracks = Vector.fill(10)(getTrack(userID))

      val nearLat       = 46.200403
      val nearLng       = 6.144115
      val location      = Location(nearLat, nearLng)
      val desiredAmount = 7
      val filter        = TrackFilter(userID, desiredAmount, nearLat, nearLng)

      val othersFarQuantity = 30
      val othersFarTracks   = Vector.fill(othersFarQuantity)(getTrack())

      val othersNearQuantity = desiredAmount
      val othersNearTracks   = Vector.fill(othersNearQuantity)(getTrack().copy(loc = location))

      val allTracks = Random.shuffle(userTracks ++ othersFarTracks ++ othersNearTracks)
      allTracks.map(dao.save).map(await(_))

      val othersTracksRetrieved = await(dao.findShared(filter))

      othersTracksRetrieved should have length desiredAmount
      othersTracksRetrieved foreach { t =>
        t.userID should not be userID
        t.loc shouldBe location
      }
    }
    "retrieves all tracks of a user" in new WithDAO[TrackDAO]("save_and_retrieve_all_tracks") {
      val userID = UUID.randomUUID()
      val track1 = getTrack(userID)
      val track2 = getTrack(userID)

      await(dao.save(track1))
      await(dao.save(track2))

      val allTracksFound = await(dao.findAll(userID))
      allTracksFound.toSet shouldBe Set(track1, track2)
    }
    "not retrieves deleted track" in new WithDAO[TrackDAO]("save_remove_and_retrieve_track") {
      val track = getTrack()
      await(dao.save(track))
      await(dao.remove(track.trackID, track.userID))

      val trackFound = await(dao.find(track.trackID))
      trackFound shouldBe None
    }
  }

}
