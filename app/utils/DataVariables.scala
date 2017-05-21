package utils

import models.{Category, PictureFingerPrint, UserCategory}
import utils.Files.ls


object DataVariables extends Logger {

  val absolutePathToData: String = "/home/snap/data/"

  private val validationDir = absolutePathToData + "validation/"
  private val sampleDir = absolutePathToData + "sample/"

  lazy val categories: Set[String] = ls(validationDir).map(_.getName).toSet

  lazy val validationCategories: Stream[Category] = {
    log("Building validation set...")
    dataSetFromFolder(validationDir)
  }
  lazy val sampleCategories: Stream[Category] = {
    log("Building sample set...")
    dataSetFromFolder(sampleDir)
  }

  def computeValidationCategory(categoryName: String): Category = {
    computeCategory(categoryName, validationDir + categoryName)
  }
  def computeSampleCategory(categoryName: String): Category = {
    computeCategory(categoryName, sampleDir + categoryName)
  }

  def pathToFolder(fbID: String, cardID: String): String =
    absolutePathToData + s"users/$fbID/$cardID/"

  def pathToImage(fbID: String, cardID: String, fileName: String): String =
    absolutePathToData + s"users/$fbID/$cardID/$fileName"


  private def computeCategory(name: String, path: String): Category = {
    log(s"Building category $name from $path")
    UserCategory(name, ls(path).map(PictureFingerPrint.fromImageFile).toSet)
  }

  private def dataSetFromFolder(dir: String): Stream[Category] = {
    ls(dir).filter(_.isDirectory).toStream.map{ catFolder =>
      log(s"Building category ${catFolder.getName} from ${catFolder.getAbsolutePath}")
      val images = ls(catFolder).filter(_.isFile)
      UserCategory(catFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }
  }

}
