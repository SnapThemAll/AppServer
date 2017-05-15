package computing

import models.PictureFingerPrint
import utils.Files.ls

object ComputingUtils {

  lazy val validationSet: Set[Category] = dataSetFromFolder("data/validation/")

  lazy val userSet: Set[Category] = dataSetFromFolder("data/user/")
  lazy val userClutterSet: Set[Category] = dataSetFromFolder("data/userClutter/")

  private def dataSetFromFolder(dir: String): Set[Category] = {
    (for {
      cardFolder <- ls(dir).filter(_.isDirectory)
    } yield {
      val images = ls(cardFolder).filter(_.isFile)
      Category(cardFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }).toSet
  }

}
