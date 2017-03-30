package controllers

import com.mohiva.play.silhouette.api.Logger
import com.mohiva.play.silhouette.impl.providers.OAuth2Constants
import models.User
import models.daos.UserDAO
import testutils.{WithApp, WithDAO}

import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class SocialAuthTest extends PlaySpec with OAuth2Constants with Logger with WithApp {

  implicit val jsonFormat       = Json.format[User]
  val supportedSocialProvider   = "facebook"
  val unsupportedSocialProvider = "madeUpProvider"

  val validToken   = "0"
  val inValidToken = "5"

  "Authenticating with a social provider" should {
    "redirect user if provider is supported" in {
      val authenticateRequest = FakeRequest(routes.SocialAuth.authenticate(supportedSocialProvider))
      val Some(result)        = route(app, authenticateRequest)

      status(result) shouldBe SEE_OTHER
    }
    "returns 401 - Not Found if provider is not supported" in {
      val authenticateRequest = FakeRequest(routes.SocialAuth.authenticate(unsupportedSocialProvider))
      val Some(result)        = route(app, authenticateRequest)

      status(result) shouldBe NOT_FOUND
    }
  }

  "Authenticating with a token" should {
    "return status 200 if token is valid and user added" in new WithDAO[UserDAO]("authenticating_with_token") {
      val Some(result) = route(
          app,
          FakeRequest(routes.SocialAuth.authenticateToken(Fake.socialProvider)).withHeaders(AccessToken -> validToken)
      )

      val user     = contentAsJson(result).as[User]
      val userInDB = await(dao.find(user.userID)).get

      status(result) shouldBe OK
      user shouldBe userInDB
    }
    "return status 401 if token is invalid" in {
      val Some(result) = route(
          app,
          FakeRequest(routes.SocialAuth.authenticateToken(Fake.socialProvider))
            .withHeaders(AccessToken -> inValidToken)
      )
      status(result) shouldBe NOT_FOUND
    }
    "returns 401 - Not Found if no tokens are found" in {
      val authenticateRequest =
        FakeRequest(routes.SocialAuth.authenticateToken(supportedSocialProvider))
      val Some(result)        = route(app, authenticateRequest)

      status(result) shouldBe NOT_FOUND
    }
    "returns 401 - Not Found if provider is not supported" in {
      val authenticateRequest =
        FakeRequest(routes.SocialAuth.authenticateToken(unsupportedSocialProvider)).withHeaders(AccessToken -> validToken)
      val Some(result)        = route(app, authenticateRequest)

      status(result) shouldBe NOT_FOUND
    }
  }
}
