package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.services.CardService
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Controller
import utils.auth.DefaultEnv

import scala.concurrent.Future
import scala.util.Random

/**
  * Class that handles the request related to a track
  * (save, get, getAll, getPredicted, getAnalysis, remove)
  *
  * Check conf.routes to see what URL to use for your requests
  */
class CardController @Inject()(configuration: Configuration ,cardService: CardService, silhouette: Silhouette[DefaultEnv]) extends Controller {

  import Utils._

  def uploadPicture(cardName: String) = silhouette.UserAwareAction.async(parse.multipartFormData) { implicit request =>

    implicit val uploadModelFormat = UploadResponse.modelFormat

    verifyAuthentication(request) { identity =>
      Future.successful(request.body.file("picture").map { picture =>
        import java.io.File
        val filename = picture.filename
        val contentType = picture.contentType
        val providerKey = identity.loginInfo.providerKey
        val absPathToSave = configuration.getString("my.data.path").getOrElse("/tmp/")
        val pictureFolderURI = absPathToSave + s"$providerKey/$cardName/"
        val pictureURI = pictureFolderURI + filename

        new File(pictureFolderURI).mkdirs()
        picture.ref.moveTo(new File(pictureURI))
        Ok(Json.toJson(UploadResponse.randomScore))
      }.getOrElse {
        UnprocessableEntity("Could not upload file")
      }
      )
      /*
      val userID = identity.userID
      val pictureURI = s"/$cardName.jpg/"
      request.body.moveTo(new File(pictureURI))
      cardService.savePicture(userID, cardName, pictureURI).map( success => {
        Ok("File uploaded, your score is: " + Random.nextDouble()*10)
      })
      */
    }.recover(recoverFromInternalServerError)
  }


}

case class UploadResponse(score: Double) {

}

object UploadResponse {
  def randomScore: UploadResponse = {
    UploadResponse(Random.nextDouble()*10)
  }

  implicit val modelFormat = Json.format[UploadResponse]
}
