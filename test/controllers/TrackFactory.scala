package controllers

import java.time.ZonedDateTime
import java.util.UUID

import models.TrackData
import models.analysis.TrackAnalysisSet
import models.geometry.Point

import scala.util.Random
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.FakeRequest

object TrackFactory {

  final val NUM_POINTS = 5

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

  def trackWithAddTrackRequest(implicit cookie: Cookie) = {
    val track = createTrackData(NUM_POINTS)
    track -> FakeRequest(routes.CardController.savePicture()).withCookies(cookie).withJsonBody(Json.toJson(track))
  }
  def invalidTrackWithAddTrackRequest(implicit cookie: Cookie) = {
    val track = createTrackData(NUM_POINTS)
    (track,
     FakeRequest(routes.CardController.savePicture())
       .withCookies(cookie)
       .withJsonBody(Json.toJson("{incorrect json format for the track}")))
  }

  def getTrackRequest(track: TrackData)(implicit cookie: Cookie) =
    FakeRequest(routes.CardController.getTrack(track.trackID.toString)).withCookies(cookie)

  def getAllTrackRequest(implicit cookie: Cookie) =
    FakeRequest(routes.CardController.getAllTracks()).withCookies(cookie)

  def getAnalysisRequest(track: TrackData)(implicit cookie: Cookie) =
    FakeRequest(routes.CardController.getAnalysis(track.trackID.toString)).withCookies(cookie)

  def getPredictedTracksRequest(implicit cookie: Cookie) =
    FakeRequest(routes.CardController.getPredictedTracks()).withCookies(cookie)

  def removeTrackRequest(track: TrackData)(implicit cookie: Cookie) =
    FakeRequest(routes.CardController.removeTrack(track.trackID.toString)).withCookies(cookie)

}
