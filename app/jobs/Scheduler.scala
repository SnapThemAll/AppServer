package jobs

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import com.google.inject.name.Named
import utils.Logger

import scala.concurrent.duration._

import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Schedules the recurrent jobs.
  */
class Scheduler @Inject()(system: ActorSystem,
                          configuration: Configuration,
                          @Named("auth-token-cleaner") authTokenCleaner: ActorRef)
    extends Logger {

  private[this] val interval = configuration.getMilliseconds("jobs.tokencleaner.interval").getOrElse(3600000l).millis
  val scheduleCleaner        = system.scheduler.schedule(0.second, interval, authTokenCleaner, AuthTokenCleaner.Clean)
}
