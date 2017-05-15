package utils

import computing.Category
import models.PictureFingerPrint
import utils.Files.ls


object EnvironmentVariables {

  val absolutePathToData: String = "/home/snap/data/"

  lazy val validationSet: Set[Category] = dataSetFromFolder(absolutePathToData + "validation")

  def pathToFolder(fbID: String, cardID: String): String =
    absolutePathToData + s"$fbID/$cardID/"
  def pathToImage(fbID: String, cardID: String, fileName: String): String =
    absolutePathToData + s"$fbID/$cardID/$fileName"

  private def dataSetFromFolder(dir: String): Set[Category] = {
    (for {
      cardFolder <- ls(dir).filter(_.isDirectory)
    } yield {
      val images = ls(cardFolder).filter(_.isFile)
      Category(cardFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }).toSet
  }

}
