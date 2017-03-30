package controllers

import java.util.UUID

import com.mohiva.play.silhouette.impl.providers.OAuth2Constants
import models.TrackData
import testutils.WithLogin
import utils.xml.XMLParser

import play.api.libs.json.Json
import play.api.test.Helpers._

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class TrackControllerTest extends PlaySpec with OAuth2Constants {

  import TrackFactory._

  "A user" should {
    "add 2 tracks and get them back correctly with getTrack and getAllTracks" in new WithLogin(
        "controller_add_two_tracks_and_retrieve") {
      implicit val cookie = loggedCookie

      val (track1, addRequest1) = trackWithAddTrackRequest
      val (track2, addRequest2) = trackWithAddTrackRequest

      val Some(addTrack1) = route(app, addRequest1)
      val Some(addTrack2) = route(app, addRequest2)

      status(addTrack1) shouldBe OK
      status(addTrack2) shouldBe OK

      //here 2 tracks added
      val Some(requestTrack1) = route(app, getTrackRequest(track1))
      val trackRequested1     = contentAsJson(requestTrack1).as[TrackData]

      status(requestTrack1) shouldBe OK
      trackRequested1 shouldBe track1

      //we can get back a track
      val Some(requestAllTracks) = route(app, getAllTrackRequest)
      val allTracksRequested     = contentAsJson(requestAllTracks).as[List[UUID]].toSet
      val allTrackId             = Set(track1.trackID, track2.trackID)

      status(requestAllTracks) shouldBe OK
      allTracksRequested shouldBe allTrackId
    }
    "add a track and remove it correctly" in new WithLogin("controller_add_track_and_remove") {
      implicit val cookie = loggedCookie

      val (track, addRequest) = trackWithAddTrackRequest
      val Some(addTrack)      = route(app, addRequest)
      status(addTrack) shouldBe OK

      val getAddedTrackRequest = getTrackRequest(track)
      val Some(requestTrack)   = route(app, getAddedTrackRequest)
      val trackRequested       = contentAsJson(requestTrack).as[TrackData]
      status(requestTrack) shouldBe OK
      trackRequested shouldBe track
      //until here everything went normally, we added the track and retrieves it correctly

      val Some(removeTrack) = route(app, removeTrackRequest(track))
      status(removeTrack) shouldBe OK

      // now that the track is deleted we should NOT be able to retrieve it
      val Some(requestTrackAgain) = route(app, getAddedTrackRequest)
      status(requestTrackAgain) shouldBe NOT_FOUND
    }
    "get an error while requesting a track if he's not logged in" in new WithLogin(
        "controller_trying_to_add_track_2338") {
      implicit val invalidCookie = loggedCookie.copy(value = "fake_cookie")

      val (track1, addRequest1) = trackWithAddTrackRequest
      val Some(addTrack1)       = route(app, addRequest1)
      status(addTrack1) shouldBe UNAUTHORIZED

      val Some(requestTrack1) = route(app, getTrackRequest(track1))
      status(requestTrack1) shouldBe UNAUTHORIZED

      val Some(requestAllTrack) = route(app, getAllTrackRequest)
      status(requestAllTrack) shouldBe UNAUTHORIZED
    }
    "get an error if format is not valid when adding a track" in new WithLogin("controller_trying_to_add_track_3838") {
      implicit val cookie = loggedCookie

      val (track1, addRequest1) = invalidTrackWithAddTrackRequest
      val Some(addTrack1)       = route(app, addRequest1)

      status(addTrack1) shouldBe UNPROCESSABLE_ENTITY
    }
    "get an error while requesting a track that is not in the database" in new WithLogin(
        "controller_trying_to_retrieve_track") {
      implicit val cookie = loggedCookie

      val Some(requestTrack) = route(app, getTrackRequest(createTrackData(NUM_POINTS)))
      status(requestTrack) shouldBe NOT_FOUND
    }
    "get an error while requesting a track that he doesn't own" in new WithLogin(
        "controller_add_track_and_retrieve_2839") {
      implicit val cookie = loggedCookie
      val cookie2         = Fake.userAndCookie(1)(app)._2

      // user 1 add the track and get it back
      val (track1, addRequest1) = trackWithAddTrackRequest
      val Some(addTrack1)       = route(app, addRequest1)
      status(addTrack1) shouldBe OK
      val Some(requestTrack1) = route(app, getTrackRequest(track1))
      status(requestTrack1) shouldBe OK

      // user 2 cannot access the track
      val Some(requestTrack2) = route(app, getTrackRequest(track1)(cookie2))
      status(requestTrack2) shouldBe NOT_FOUND
    }
    "get the analysis back of an added track" in new WithLogin("controller_add_track_and_get_analysis") {
      implicit val cookie = loggedCookie

      val (track, addRequest) = trackWithAddTrackRequest
      val Some(addTrack)      = route(app, addRequest)
      status(addTrack) shouldBe OK

      val Some(analysisResult) = route(app, getAnalysisRequest(track))

      status(analysisResult) shouldBe OK
      contentAsString(analysisResult) should not be ""
    }
    "get some predicted tracks" in new WithLogin("controller_add_track_and_get_predicted_tracks") {
      implicit val cookie = loggedCookie
      val cookie2         = loggedCookie2
      val amount          = 5
      val filter = Json.obj(
          "amount"    -> amount,
          "minLength" -> 0
      )

      (0 to 10).foreach { _ =>
        val Some(addTrack) = route(app, trackWithAddTrackRequest._2)
        status(addTrack) shouldBe OK
        val Some(addTrack2) = route(app, trackWithAddTrackRequest(cookie2)._2)
        status(addTrack2) shouldBe OK
      }

      val Some(predictedTracks) = route(app, getPredictedTracksRequest.withJsonBody(filter))
      status(predictedTracks) shouldBe OK
      lazy val predictedTracksParsed = XMLParser.nameDatePointsFromString(contentAsString(predictedTracks))
      noException should be thrownBy predictedTracksParsed
      predictedTracksParsed.length shouldBe amount
    }
  }

}
