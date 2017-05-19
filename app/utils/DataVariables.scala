package utils

import models.{Category, PictureFingerPrint, UserCategory}
import utils.Files.ls


object DataVariables {

  val absolutePathToData: String = "/home/snap/data/"

  private val validationDir = absolutePathToData + "validation"
  private val sampleDir = absolutePathToData + "sample"

  lazy val validationSet: Set[Category] = dataSetFromFolder(validationDir)
  lazy val sampleSet: Set[Category] = dataSetFromFolder(sampleDir)

  def pathToFolder(fbID: String, cardID: String): String =
    absolutePathToData + s"users/$fbID/$cardID/"

  def pathToImage(fbID: String, cardID: String, fileName: String): String =
    absolutePathToData + s"users/$fbID/$cardID/$fileName"

  private def dataSetFromFolder(dir: String): Set[Category] = {
    (for {
      cardFolder <- ls(dir).filter(_.isDirectory)
    } yield {
      val images = ls(cardFolder).filter(_.isFile)
      UserCategory(cardFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }).toSet
  }

}
