package jobs

import javax.inject.Inject

import akka.actor._
import com.mohiva.play.silhouette.api.util.Clock
import models.AuthToken
import models.services.AuthTokenService
import utils.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A job which cleanup invalid auth tokens.
  *
  * @param service The auth token service implementation.
  * @param clock The clock implementation.
  */
class AuthTokenCleaner @Inject()(service: AuthTokenService, clock: Clock) extends Actor with Logger {
  import AuthTokenCleaner._

  /**
    * Process the received messages.
    */
  def receive: Receive = {
    case Clean =>
      val start = clock.now.getMillis
      val msg   = new StringBuffer("\n")
      msg.append("=================================\n")
      msg.append("Start to cleanup auth tokens\n")
      msg.append("=================================\n")

      sender ! Done(
          service.clean.map { deleted =>
            val seconds = (clock.now.getMillis - start) / 1000
            msg
              .append("Total of %s auth tokens(s) were deleted in %s seconds".format(deleted.length, seconds))
              .append("\n")
            msg.append("=================================\n")

            msg.append("=================================\n")
            log(msg.toString)
            deleted
          }.recover {
            case e =>
              msg.append("Couldn't cleanup auth tokens because of unexpected error\n")
              msg.append("=================================\n")
              error(msg.toString, e)
              throw e
          }
      )
  }
}

/**
  * The companion object.
  */
object AuthTokenCleaner {

  def props = Props[AuthTokenCleaner]

  case object Clean

  case class Done(deleted: Future[Seq[AuthToken]])
}
