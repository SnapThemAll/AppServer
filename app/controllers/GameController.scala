package controllers

import java.io.File
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, AnyContent, Controller}
import utils.auth.DefaultEnv

import scala.concurrent.Future

class GameController @Inject()(configuration: Configuration, silhouette: Silhouette[DefaultEnv]) extends Controller {

  import Utils._
  val absPathToSave: String = configuration.getString("my.data.path").getOrElse("/tmp/")

  def getLevels: Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit request =>
      verifyAuthentication(request) { identity =>
        val pathToFile = absPathToSave + "levels.json"
        val file = new File(pathToFile)
        if (file.exists()) {
          Future.successful(Ok.sendFile(new File(absPathToSave)))
        } else {
          Future.successful(NotFound)
        }
      }.recover(recoverFromInternalServerError)
    }

}
