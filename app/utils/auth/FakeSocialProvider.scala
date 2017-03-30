package utils.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{ExtractableRequest, HTTPLayer}
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import models.User

import scala.concurrent.Future
import scala.util.Try

import play.api.mvc.{Result, Results}

/**
  * <p>The Fake Provider. This provider is used only for testing purposes. It simulates the process of retrieving a
  * user from a social provider with its token already known. The provider has only 5 users in total and there's a
  * direct mapping from the token to the n'th user (user with token 0 is the first user, ...).</p>
  * <p>It expects the following strings in the request:<br>
  *     - access token <br>
  * It expects the name of this parameter as defined in [[OAuth2Constants]]
  * </p>
  *
  * @param httpLayer     The HTTP layer implementation.
  * @param stateProvider The state provider implementation.
  * @param settings      The provider settings.
  */
class FakeSocialProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, settings: OAuth2Settings)
    extends FacebookProvider(httpLayer, stateProvider, settings) with Results {

  import FakeSocialProvider._

  override val id: String = "fake"

  /**
    * Starts the authentication process.
    *
    * @param request The current request.
    * @tparam B The type of the request body.
    * @return Either a Result or the auth info from the provider.
    */
  override def authenticate[B]()(implicit request: ExtractableRequest[B]): Future[Either[Result, OAuth2Info]] = {
    request.extractString(AccessToken) match {
      case Some(token) if isValidToken(token) =>
        Future(
            Right(
                OAuth2Info(accessToken = token)
            ))
      case _ =>
        Future(
            Left(
                NotFound("Fake provider has only 5 users. Provide a token ranging from 0 to 4")
            ))
    }
  }

  /**
    * Builds the social profile.
    *
    * @param authInfo The auth info received from the provider.
    * @return On success the build social profile, otherwise a failure.
    */
  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    if (isValidToken(authInfo.accessToken)) {
      Future {
        val user = users(authInfo.accessToken.toInt)
        CommonSocialProfile(
            loginInfo = user.loginInfo,
            firstName = user.firstName,
            lastName = user.lastName,
            fullName = user.name,
            avatarURL = user.avatarURL,
            email = user.email
        )
      }
    } else {
      Future.failed(
          new ProfileRetrievalException("Fake provider has only 5 users. Provide a token ranging from 0 to 4")
      )
    }
  }
}

object FakeSocialProvider {

  private[this] val nbUsers = 5

  private[this] val ids = Vector(
      "ef6a3a57-af7d-4b19-8ba3-1105c3aba203",
      "b4441f64-3053-41c7-982a-4deeeb07e9b3",
      "c9ed2206-0f37-4edc-b70e-968f666bf877",
      "b646af94-ba8c-4e79-863b-501480b8d8b2",
      "f1322d7b-5cc4-41e5-8171-3030eb7fcbd9"
  )

  assert(ids.size == nbUsers, s"Should have at least $nbUsers ids to generate as much fake users")

  private val users = Vector.tabulate(nbUsers)(
      n =>
        User(UUID.fromString(ids(n)),
             LoginInfo("fake", ('a' + n).toChar + "@fake.fake"),
             Some("FirstName" + ('A' + n).toChar),
             Some("LastName" + ('A' + n).toChar),
             Some(('a' + n).toChar + "@fake.fake"),
             None,
             true)
  )

  def isValidToken(token: String): Boolean = Try(token.toInt).isSuccess && (0 until nbUsers).contains(token.toInt)
}
