package computing

import models.{PictureFingerPrint, UserCategory}
import utils.Files.ls

object ComputingUtils {

  lazy val validationSet: Set[UserCategory] = dataSetFromFolder("data/validation/")

  lazy val userSet: Set[UserCategory] = dataSetFromFolder("data/user/")
  lazy val userClutterSet: Set[UserCategory] = dataSetFromFolder("data/userClutter/")

  private def dataSetFromFolder(dir: String): Set[UserCategory] = {
    (for {
      cardFolder <- ls(dir).filter(_.isDirectory)
    } yield {
      val images = ls(cardFolder).filter(_.isFile)
      UserCategory(cardFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }).toSet
  }

}
