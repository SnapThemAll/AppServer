package controllers

import java.time.ZonedDateTime
import java.util.UUID

import com.mohiva.play.silhouette.impl.providers.OAuth2Constants
import models.TrackData
import models.analysis._
import models.geometry.Point
import models.profile.UserProfile
import org.jfree.util.Log
import org.scalatools.testing.Logger
import testutils.WithLogin
import utils.xml.{XMLParser, XMLWriter}

import scala.concurrent.Future
import scala.util.Random

import play.api
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsJson, Cookie, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Result

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._

class UserProfileControllerTest extends PlaySpec with OAuth2Constants{

  import TrackFactory._

  "A user" should {
    "get his profile related to the track he added" in new WithLogin("user_gets_his_profile") {
      implicit val cookie = loggedCookie

      def addTrack(implicit cookie: Cookie): TrackData = {
        val (track, request)       = trackWithAddTrackRequest
        val Some(addedTrackResult) = route(app, request)
        status(addedTrackResult) shouldBe OK

        track
      }

      val addedTracks = List.tabulate(4)(_ => {
        val track = addTrack.toTrack(loggedUser.userID)
        val analyzer = TrackAnalyzer(track)
        val analyses = TrackAnalysis.allTrackObjects.map{
          _.apply(analyzer)
        }

        analyses.foldLeft(track)(_.addAnalysis(_))
      } )
      val expectedProfile = UserProfile(addedTracks)

      eventually (timeout(5 seconds), interval(1000 millis)) {
        val Some(profileResult) = route(app, FakeRequest(routes.UserProfileController.getProfile()).withCookies(cookie))
        contentAsString(profileResult) shouldBe expectedProfile.toXML.toString
      }
    }
  }

}
