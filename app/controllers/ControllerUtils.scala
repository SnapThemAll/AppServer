package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.services.UpdateService
import utils.Logger
import utils.auth.DefaultEnv

import scala.concurrent.Future
import play.api.mvc.Result
import play.api.mvc.Results._

class ControllerUtils @Inject()(updateService: UpdateService) extends Logger {

  def verifyAuthentication[T](appVersion: String)(request: => UserAwareRequest[DefaultEnv, T])(
    whatToDo: => (DefaultEnv#I => Future[Result])): Future[Result] = {

    request.identity match {
      case Some(identity) => // WE ARE LOGGED IN
        Future.sequence(
          List(
            updateService.userNeedToUpdateApp(appVersion),
            updateService.serverIsUpdating()
          )
        ).flatMap {
          case true :: tail => // APP IS OUT OF DATE
            Future.successful(
              Forbidden(
                "You need to update the app to get your score. " +
                  "New features and better scoring is waiting for you :)")
            )

          case false :: true :: Nil => // SERVER IS UPDATING
            Future.successful(
              Forbidden(
                "Server is being updated... " +
                  "You can still take snaps. " +
                  "Then to compute their score at the same time, go to the 'Settings' tab.")
            )
          case _ => //EVERYTHING IS FINE
            whatToDo(identity)
        }

      case None =>
        Future.successful(
          Forbidden("It seems you are not logged in. " +
            "Please sign out and sign in again. " +
            "(note that you must accept to share your friends' list and e-mail)")
        )
    }
  }

  def recoverWhenRetrievingPicture: PartialFunction[Throwable, Result] = {
    val notFound = NotFound("Picture not found. Verify 'cardID' and 'fileName'")
    PartialFunction[Throwable, Result] {
      case _: NoSuchElementException => notFound
      case _: IllegalAccessException => notFound
    } orElse recoverFromInternalServerError
  }

  def recoverFromInternalServerError: PartialFunction[Throwable, Result] =
    PartialFunction[Throwable, Result] { t =>
      error("Error while answering a request", t)
      InternalServerError("An internal server error occurred. We are working on it.")
    }
}
