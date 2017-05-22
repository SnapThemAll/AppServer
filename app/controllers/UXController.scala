package controllers

import java.io.File
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.reponses.{PictureDataResponse, PictureUploadResponse, ScoreResponse}
import models.Descriptor
import models.services.{CardService, FeedbackService}
import play.api.libs.Files
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller, MultipartFormData}
import utils.DataVariables
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
  * Class that handles the request related to a card
  *
  * Check conf.routes to see what URL to use for your requests
  */
class UXController @Inject()(feedbackService: FeedbackService, silhouette: Silhouette[DefaultEnv]) extends Controller {

  import Utils._

  def uploadFeedback: Action[String] =
    silhouette.UserAwareAction.async(parse.text) { implicit request =>
      verifyAuthentication(request) { identity =>
        val msg = request.body
        feedbackService.save(msg, identity.loginInfo.providerKey)
          .map(_ => Ok)
      }.recover(recoverFromInternalServerError)
    }

}