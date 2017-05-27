package controllers

import java.io.File
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, AnyContent, Controller}
import utils.DataVariables
import utils.auth.DefaultEnv

import scala.concurrent.Future

class GameController @Inject()(controllerUtils: ControllerUtils, silhouette: Silhouette[DefaultEnv]) extends Controller {

  import controllerUtils._
  val pathToFile: String = DataVariables.absolutePathToData + "levels.json"

  def getLevels(appVersion: String): Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(appVersion)(request) { identity =>
        val file = new File(pathToFile)
        if (file.exists()) {
          Future.successful(Ok.sendFile(file))
        } else {
          Future.successful(NotFound)
        }
      }.recover(recoverFromInternalServerError)
    }
}
