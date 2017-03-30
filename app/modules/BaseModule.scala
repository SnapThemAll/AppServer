package modules

import com.google.inject.AbstractModule
import models.daos._
import models.services._
import net.codingwell.scalaguice.ScalaModule

import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * The base Guice module.
  */
class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  /**
    * Configures the module.
    */
  def configure(): Unit = {
    bind[UserDAO].to[UserDAOMongo]
    bind[AuthTokenDAO].to[AuthTokenDAOMongo]
    bind[TrackDAO].to[TrackDAOMongo]

    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[UserService].to[UserServiceImpl]
    bind[TrackService].to[TrackServiceImpl]
    bind[UserProfileService].to[UserProfileServiceImpl]
  }
}
