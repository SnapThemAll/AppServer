package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.services.UserProfileService
import utils.auth.DefaultEnv

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller

/**
  * Class that handles the request related to the profile of a user
  */
class UserProfileController @Inject()(userProfileService: UserProfileService, silhouette: Silhouette[DefaultEnv])
    extends Controller {

  import Utils._

  /**
    * Handles an authenticated request to get the profile of the user asking it
    *
    * @return An Action that handles a Request and generates a Result to be sent to the client.
    *         The Result will be 200 with the profile (as XML) if everything went fine.
    *         Otherwise : 401 if the user is not authenticated and 500 if there is an InternalServerError.
    */
  def getProfile = silhouette.UserAwareAction.async { implicit request =>
    verifyAuthentication(request) { identity =>
      userProfileService.compute(identity.userID).map { userProfile =>
        Ok(userProfile.toXML.toString)
      }
    }.recover(recoverFromInternalServerError)
  }
}
