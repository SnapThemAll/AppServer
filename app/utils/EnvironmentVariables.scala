package utils

import models.{PictureFingerPrint, UserCategory}
import utils.Files.ls


object EnvironmentVariables {

  val absolutePathToData: String = "/home/snap/data/"
  val validationDir = absolutePathToData + "validation"
  val samplesDir = absolutePathToData + "sample"

  val validationSet: Set[UserCategory] = dataSetFromFolder(absolutePathToData + "validation")

  def pathToFolder(fbID: String, cardID: String): String =
    absolutePathToData + s"users/$fbID/$cardID/"
  def pathToImage(fbID: String, cardID: String, fileName: String): String =
    absolutePathToData + s"users/$fbID/$cardID/$fileName"

  private def dataSetFromFolder(dir: String): Set[UserCategory] = {
    (for {
      cardFolder <- ls(dir).filter(_.isDirectory)
    } yield {
      val images = ls(cardFolder).filter(_.isFile)
      UserCategory(cardFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }).toSet
  }

}
