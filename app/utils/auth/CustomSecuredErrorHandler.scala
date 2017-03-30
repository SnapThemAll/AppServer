package utils.auth

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler

import scala.concurrent.Future

import play.api.mvc.RequestHeader
import play.api.mvc.Results._

/**
  * Custom secured error handler.
  */
class CustomSecuredErrorHandler extends SecuredErrorHandler {

  /**
    * Called when a user is not authenticated.
    *
    * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthenticated(implicit request: RequestHeader) = {
    Future.successful(Unauthorized)
  }

  /**
    * Called when a user is authenticated but not authorized.
    *
    * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthorized(implicit request: RequestHeader) = {
    Future.successful(Forbidden)
  }
}
