package modules

import com.google.inject.AbstractModule
import models.daos._
import models.daos.mongo._
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
    bind[AuthTokenDAO].to[AuthTokenDAOMongo]
    bind[UserDAO].to[UserDAOMongo]
    bind[CardDAO].to[CardDAOMongo]
    bind[ValidationCategoryDAO].to[ValidationCategoryDAOMongo]
    bind[FeedbackDAO].to[FeedbackDAOMongo]
    bind[UpdateDAO].to[UpdateDAOMongo]

    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[UserService].to[UserServiceImpl]
    bind[CardService].to[CardServiceImpl]
    bind[FeedbackService].to[FeedbackServiceImpl]
    bind[UpdateService].to[UpdateServiceImpl]
  }
}
