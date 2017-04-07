package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{Logger, LoginEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.{OAuth2StateException, ProfileRetrievalException}
import com.mohiva.play.silhouette.impl.providers._
import models.User
import models.services.UserService
import utils.auth.{DefaultEnv, OAuthInfoFromToken}

import scala.concurrent.Future
import scala.util.control.NonFatal

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
  * The social auth controller.
  *
  * @param silhouette The Silhouette stack.
  * @param userService The user service implementation.
  * @param authInfoRepository The auth info service implementation.
  * @param socialProviderRegistry The social provider registry.
  */
class SocialAuth @Inject()(silhouette: Silhouette[DefaultEnv],
                           val userService: UserService,
                           authInfoRepository: AuthInfoRepository,
                           socialProviderRegistry: SocialProviderRegistry)
    extends Controller with Logger {

  import User.jsonFormat

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) =>
            for {
              profile       <- p.retrieveProfile(authInfo)
              user          <- userService.save(profile)
              authInfo      <- authInfoRepository.save(profile.loginInfo, authInfo)
              authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
              value         <- silhouette.env.authenticatorService.init(authenticator)
              result        <- silhouette.env.authenticatorService.embed(value, Redirect(routes.Application.index()))
            } yield {
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              result
            }
        }

      case _ =>
        Future.failed(
            new ProviderException(s"Cannot authenticate with unexpected " +
              s"social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        NotFound(e.getMessage)
    }
  }

  /**
    * Authenticates a user against a social provider using a token he already possesses.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticateToken(provider: String) = Action.async { implicit request =>
    ((socialProviderRegistry.get[SocialProvider](provider), OAuthInfoFromToken()) match {
      case (Some(p: OAuth2Provider with CommonSocialProfileBuilder), Some(authInfo)) => //Success !
        for {
          profile       <- p.retrieveProfile(authInfo)
          user          <- userService.save(profile)
          authInfo      <- authInfoRepository.save(profile.loginInfo, authInfo)
          authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
          value         <- silhouette.env.authenticatorService.init(authenticator)
          result        <- silhouette.env.authenticatorService.embed(value, Ok(Json.toJson(user)))
        } yield {
          silhouette.env.eventBus.publish(LoginEvent(user, request))
          result
        }

      //Otherwise fail
      case (Some(p: SocialProvider with CommonSocialProfileBuilder), Some(_)) =>
        Future.failed(
            new ProviderException(s"Cannot authenticate using token with specified " +
              s"provider $provider: not an OAuth2 provider"))
      case (None, Some(_)) =>
        Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
      case (_, None) =>
        Future.failed(
            new OAuth2StateException(
                s"No token found in the request while authenticating with $provider. " +
                  s"Expected token name: ${OAuthInfoFromToken.AccessToken}")
        )
      case (_, t) =>
        Future.failed(
            new Exception(s"Cannot authenticate with social provider $provider " +
              s"and token $t: unexpected exception"))
    }).recover {
      case NonFatal(e) =>
        val errorLog = e match {
          case _: ProfileRetrievalException => "Invalid token error" //Couldn't find a profile with such token
          case _: OAuth2StateException      => "Unexpected token error"
          case _: ProviderException         => "Unexpected provider error"
          case _                            => "Unexpected error"
        }
        logger.error(errorLog, e)
        NotFound(e.getMessage)
    }
  }
}
