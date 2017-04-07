package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import models.User.jsonFormat
import utils.auth.DefaultEnv

import scala.language.postfixOps

import play.api.libs.json.Json
import play.api.mvc.Controller

class Application @Inject()(silhouette: Silhouette[DefaultEnv]) extends Controller {

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.Application.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  /**
    * Renders the index page. Knows if the user is logged in or not
    *
    * @return The result to send to the client.
    */
  def index = silhouette.UserAwareAction { implicit request =>
    val userDesc = request.identity match {
      case Some(identity) => identity.name getOrElse "Unnamed user"
      case None           => "Guest"
    }
    Ok(s"Dear $userDesc")
  }

  def isAuthenticated =
    silhouette.UserAwareAction(implicit request =>
      request.identity match {
        case Some(identity) => Ok(Json.toJson(identity))
        case None           => Unauthorized
    })

}
