package controllers

import java.io.File
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.reponses.{PictureDataResponse, PictureUploadResponse, ScoreResponse}
import models.Descriptor
import models.services.CardService
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
class CardController @Inject()(cardService: CardService, silhouette: Silhouette[DefaultEnv]) extends Controller {

  import Utils._

  def uploadPicture(cardID: String): Action[MultipartFormData[Files.TemporaryFile]] =
    silhouette.UserAwareAction.async(parse.multipartFormData) { implicit request =>
      implicit val uploadModelFormat = PictureUploadResponse.modelFormat

      verifyAuthentication(request) { identity =>
        request.body.file("picture").map { picture =>
          val fileName = picture.filename
          val contentType = picture.contentType
          val uuid = identity.loginInfo.providerKey

          val tmpFile = picture.ref.file

          val descriptor = Descriptor.fromImageFile(tmpFile)

          if(descriptor.descriptorMatrix.rows < 100) {
            log(s"Unprocessable entity: Quality of the image is too low (${descriptor.descriptorMatrix.rows} features)")
            Future.successful(
              UnprocessableEntity("Quality of the image is too low")
            )
          } else {
            cardService.savePicture(uuid, cardID, fileName, descriptor)
              .map{ score =>
                val pictureFolderURI = DataVariables.pathToFolder(uuid, cardID)
                val pictureFile = new File(pictureFolderURI + fileName)
                new File(pictureFolderURI).mkdirs()
                picture.ref.moveTo(pictureFile)
                Ok(Json.toJson(PictureUploadResponse(score)))
              }
          }

        }.getOrElse{
          log("Unprocessable entity: No picture file found in the request")
          Future.successful(
            BadRequest("No picture file found in the request")
          )
        }
      }.recover(recoverFromInternalServerError)
    }

  def getPicture(cardID: String, fileName: String): Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(request) { identity =>
        val uuid = identity.loginInfo.providerKey
        val pictureURI = DataVariables.pathToImage(uuid, cardID, fileName)
        cardService.retrievePicture(uuid, cardID, fileName).map{
          case Some(_) => Ok.sendFile(new File(pictureURI))
          case None => NotFound
        }
      }.recover(recoverFromInternalServerError)
    }

  def getPicturesData: Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(request) { identity =>
        cardService.retrieveAll(identity.loginInfo.providerKey)
          .map{cards =>
            cards.flatMap( PictureDataResponse.fromCard )}
          .map{pictureUploadResponses =>
            Ok(Json.toJson(pictureUploadResponses))}
      }.recover(recoverFromInternalServerError)
    }

  def getScore(fbID: String): Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(request) { identity =>
        cardService.computeTotalScore(fbID)
          .map{ score =>
            Ok(Json.toJson(ScoreResponse(score)))}
      }.recover(recoverFromInternalServerError)
    }


  def removePicture(cardID: String, fileName: String): Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(request) { identity =>
        val uuid = identity.loginInfo.providerKey
        val pictureURI = DataVariables.pathToImage(uuid, cardID, fileName)
        cardService.removePicture(uuid, cardID, fileName)
          .map{ _ =>
            //if(new File(pictureURI).delete()){
            if(new File(pictureURI).exists()) {
              Ok("Picture Deleted")
            } else {
              Ok("Picture Not Deleted")
            }
          }
      }.recover(recoverFromInternalServerError)
    }


  def getAllScores: Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(request) { identity =>
        cardService.computeTotalScoreAllUsers()
          .map{ namesAndScore =>
            Ok(
              Json.toJson(
                namesAndScore.map{ case (name, score) =>
                  ScoreResponse(score, Some(name))
                }
              )
            )
          }
      }.recover(recoverFromInternalServerError)
    }

}