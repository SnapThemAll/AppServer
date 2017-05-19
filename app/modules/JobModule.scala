package modules

import jobs.{AuthTokenCleaner, Scheduler}
import models.daos.Init
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * The job module.
  */
class JobModule extends ScalaModule with AkkaGuiceSupport {

  /**
    * Configures the module.
    */
  def configure() = {
    bindActor[AuthTokenCleaner]("auth-token-cleaner")
    bind[Scheduler].asEagerSingleton()

    bindActor[AuthTokenCleaner]("auth-token-cleaner-actor")

    bind[Init].asEagerSingleton()
  }
}
