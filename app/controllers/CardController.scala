package controllers

import java.io.File
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.services.CardService
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{BodyParsers, Controller}
import utils.auth.DefaultEnv

import scala.concurrent.Future
import scala.util.Random

/**
  * Class that handles the request related to a track
  * (save, get, getAll, getPredicted, getAnalysis, remove)
  *
  * Check conf.routes to see what URL to use for your requests
  */
class CardController @Inject()(cardService: CardService, silhouette: Silhouette[DefaultEnv]) extends Controller {

  import Utils._

  def uploadPicture(cardName: String) = silhouette.UserAwareAction.async(parse.temporaryFile) { implicit request =>
    verifyAuthentication(request) { identity =>
      val userID = identity.userID
      val pictureURI = s"/tmp/picture/$userID/$cardName/"
      request.body.moveTo(new File(pictureURI))
      cardService.savePicture(userID, cardName, pictureURI).map( success => {
        Ok("File uploaded, your score is: " + Random.nextDouble()*10)
      })
    }.recover(recoverFromInternalServerError)
  }

}
