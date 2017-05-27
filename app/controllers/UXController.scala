package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.services.FeedbackService
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import utils.auth.DefaultEnv

/**
  * Class that handles the request related to a card
  *
  * Check conf.routes to see what URL to use for your requests
  */
class UXController @Inject()(
                              controllerUtils: ControllerUtils,
                              feedbackService: FeedbackService,
                              silhouette: Silhouette[DefaultEnv]
                            ) extends Controller {

  import controllerUtils._

  def uploadFeedback(appVersion: String): Action[String] =
    silhouette.UserAwareAction.async(parse.text) { implicit request =>
      verifyAuthentication(appVersion)(request) { identity =>
        val msg = request.body
        feedbackService.save(msg, identity.loginInfo.providerKey)
          .map(_ => Ok)
      }.recover(recoverFromInternalServerError)
    }

}