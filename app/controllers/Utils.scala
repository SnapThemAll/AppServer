package controllers

import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import utils.Logger
import utils.auth.DefaultEnv

import scala.concurrent.Future

import play.api.mvc.Result
import play.api.mvc.Results._

object Utils extends Logger {

  def verifyAuthentication[T](request: => UserAwareRequest[DefaultEnv, T])(
      whatToDo: => (DefaultEnv#I => Future[Result])) = {
    request.identity match {
      case Some(identity) => whatToDo(identity)
      case None           => Future.successful(Unauthorized("Unauthorized access. Please sign-in first."))
    }
  }

  def recoverWhenRetrievingTrack = {
    val notFound = NotFound("Track not found. The trackID requested might be incorrect.")
    PartialFunction[Throwable, Result] {
      case _: NoSuchElementException => notFound
      case _: IllegalAccessException => notFound
    } orElse recoverFromInternalServerError
  }

  def recoverFromInternalServerError =
    PartialFunction[Throwable, Result] { t =>
      logger.error("Error while answering a request", t)
      InternalServerError("An internal server error occurred. We are working on it.")
    }
}
