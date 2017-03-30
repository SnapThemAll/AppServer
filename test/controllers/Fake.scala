package controllers

import com.mohiva.play.silhouette.impl.providers.OAuth2Provider.AccessToken
import models.User

import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

object Fake {


  val tokens = (0 to 4).map( _.toString )
  val socialProvider = "fake"

  def userAndCookie(n: Int)(app: play.api.Application) = authenticateWithToken(tokens(n))(app)

  private def authenticateWithToken(token: String)(app: play.api.Application) = {
    val Some(result) = route(
        app,
        FakeRequest(routes.SocialAuth.authenticateToken(socialProvider)).withHeaders(AccessToken -> token))
    val user         = contentAsJson(result).as[User]
    val Some(cookie) = Helpers.cookies(result).get("authenticator")
    assert(status(result) == OK)

    (user, cookie)
  }
}
