package controllers

import com.mohiva.play.silhouette.impl.providers.OAuth2Constants
import models.User
import testutils.{WithApp, WithLogin}

import play.api.test.Helpers._
import play.api.test._

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class ApplicationTest extends PlaySpec with OAuth2Constants with WithApp {

  val validToken         = Fake.tokens(0)
  val fakeSocialProvider = Fake.socialProvider

  "Page index" should {
    "display 'Dear Guest' if user is not authenticated" in {
      val Some(result)     = route(app, FakeRequest(routes.Application.index()))
      val bodyText: String = contentAsString(result)

      bodyText shouldBe "Dear Guest"
    }
    "display user if he's connected" in new WithLogin("user_auth_8238") {
      val Some(result)     = route(app, FakeRequest(routes.Application.index()).withCookies(loggedCookie))
      val bodyText: String = contentAsString(result)

      bodyText shouldBe "Dear " + loggedUser.name.getOrElse("Unnamed user")
    }
  }

  "The 'isAuthenticated' method" should {
    "return status 200 if the user associated with the cookie is found" in new WithLogin("user_auth_9416") {
      val isAuthenticatedRequest =
        FakeRequest(routes.Application.isAuthenticated()).withCookies(loggedCookie)
      val Some(result) = route(app, isAuthenticatedRequest)

      contentAsJson(result).as[User] shouldBe loggedUser
      status(result) shouldBe OK
    }
    "return status 401 if the cookie is not associated to any user" in new WithLogin("user_auth_7392") {
      val invalidLoginCookie = loggedCookie.copy(value = "fake_cookie")
      val isAuthenticatedRequest =
        FakeRequest(routes.Application.isAuthenticated()).withCookies(invalidLoginCookie)
      val Some(result) = route(app, isAuthenticatedRequest)

      status(result) shouldBe UNAUTHORIZED
    }
  }
}
