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
      val pictureURI = s"/tmp/picture/${userID}/${cardName}/"
      request.body.moveTo(new File(pictureURI))
      cardService.savePicture(userID, cardName, pictureURI).map( success => {
        Ok("File uploaded")
      })
    }.recover(recoverFromInternalServerError)
  }

  /**
    * Handles an authenticated request sending a track (as Json) to server to save it
    *
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 if the user successfully saved his track.
    *         Otherwise : 401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def savePicture(cardName: String) = silhouette.UserAwareAction.async(BodyParsers.parse.json) { implicit request =>
    verifyAuthentication(request) { identity =>
      request.body.asOpt[TrackData] match {
        case Some(trackData) =>
          cardService.save(identity.userID, trackData).map { trackAddedID =>
            Ok("models.Track " + trackAddedID + " correctly added")
          }
        case None => Future.successful(UnprocessableEntity("Parsing of the track failed"))
      }
    }.recover(recoverFromInternalServerError)
  }

  /**
    * Handles an authenticated request to get a track (as Json) given its trackID
    *
    * @param trackIDAsString the trackID as a UUID.toString
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 with the track (as Json) if everything went fine.
    *         Otherwise : 404 if no tracks with this trackID found or the track is not owned by this user,
    *         401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def getTrack(trackIDAsString: String) = silhouette.UserAwareAction.async { implicit request =>
    verifyAuthentication(request) { identity =>
      cardService.retrieve(identity.userID, UUID.fromString(trackIDAsString)).map { track =>
        Ok(Json.toJson(track))
      }
    }.recover(recoverWhenRetrievingTrack)
  }

  /**
    * Handles an authenticated request to get the analysis of a track (as XML) given its trackID
    *
    * @param trackIDAsString the trackID as a UUID.toString
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 with the track (as XML) if everything went fine..
    *         Otherwise : 404 if no tracks with this trackID found or the track is not owned by this user,
    *         401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def getAnalysis(trackIDAsString: String) = silhouette.UserAwareAction.async { implicit request =>
    verifyAuthentication(request) { identity =>
      cardService.retrieveAnalysis(identity.userID, UUID.fromString(trackIDAsString)).map { trackAnalysis =>
        Ok(trackAnalysis)
      }
    }.recover(recoverWhenRetrievingTrack)
  }

  /**
    * Handles an authenticated request to get all the trackIDs owned by the user requesting it
    *
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 with a list of trackIDs (as Json) if everything went fine.
    *         Otherwise : 401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def getAllTracks = silhouette.UserAwareAction.async { implicit request =>
    verifyAuthentication(request) { identity =>
      cardService.retrieveAll(identity.userID).map { trackIDs =>
        Ok(Json.toJson(trackIDs))
      }
    }.recover(recoverFromInternalServerError)
  }

  /**
    * Handles an authenticated request to get some tracks with a prediction of the time the user will take
    * running it
    *
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 with a list of tracks (as XML) if everything went fine.
    *         Otherwise : 401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def getPredictedTracks = silhouette.UserAwareAction.async { implicit request =>
    verifyAuthentication(request) { identity =>
      val filter = request.body.asJson.getOrElse(Json.obj()).as[TrackFilterData]
      cardService.retrievePredicted(filter.toTrackFilter(identity.userID)).map { predictedTracksAsXML =>
        Ok(predictedTracksAsXML)
      }
    }.recover(recoverFromInternalServerError)
  }

  /**
    * Handles an authenticated request to remove a track given its trackID
    *
    * @param trackIDAsString the trackID as a UUID.toString
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 if the track is successfully removed from the server.
    *         Otherwise : 404 if no tracks with this trackID found or the track is not owned by this user,
    *         401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def removeTrack(trackIDAsString: String) = silhouette.UserAwareAction.async { implicit request =>
    verifyAuthentication(request) { identity =>
      cardService.removePicture(identity.userID, UUID.fromString(trackIDAsString)).map { trackRemovedID =>
        Ok("models.Track " + trackRemovedID + " correctly removed")
      }
    }.recover(recoverWhenRetrievingTrack)
  }
}
