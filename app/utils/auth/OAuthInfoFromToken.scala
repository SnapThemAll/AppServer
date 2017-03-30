package utils.auth

import com.mohiva.play.silhouette.api.AuthInfo
import com.mohiva.play.silhouette.api.util.ExtractableRequest
import com.mohiva.play.silhouette.impl.providers.{OAuth2Constants, OAuth2Info, OAuth2Provider}

/**
  * Helper object to obtain an [[AuthInfo]] (more precisely an [[OAuth2Info]]) from a token provided in a request.
  */
object OAuthInfoFromToken extends OAuth2Constants {

  /**
    * Gets the [[OAuth2Info]] assuming the request contains a valid token for an [[OAuth2Provider]].
    * <p>It expects the following strings in the request:<br>
    *     - access token<br>
    *     - token type (optional)<br>
    *     - expires in (optional)<br>
    *     - refresh token (optional)<br>
    * It expects the name of these parameters as defined in [[OAuth2Constants]]
    * </p>
    *
    * @param request The current request.
    * @tparam B The type of the request body.
    * @return An optional OAuth2Info if the request contained the access token
    */
  def apply[B]()(implicit request: ExtractableRequest[B]): Option[OAuth2Info] = {
    request.extractString(AccessToken).map { token =>
      OAuth2Info(
          accessToken = token,
          tokenType = request.extractString(TokenType),
          expiresIn = request.extractString(ExpiresIn).map(_.toInt),
          refreshToken = request.extractString(RefreshToken)
      )
    }
  }
}
