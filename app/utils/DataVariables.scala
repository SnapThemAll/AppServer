package utils

import utils.Files.ls


object DataVariables extends Logger {

  val absolutePathToData: String = "/home/snap/data/"

  private val validationDir = absolutePathToData + "validation/"
  private val sampleDir = absolutePathToData + "sample/"

  lazy val categories: Set[String] = ls(validationDir).map(_.getName).toSet

  def listValidationFileNames(category: String): Set[String] =
    listFileNames(pathToValidationFolder(category))

  def listSampleFileNames(category: String): Set[String] =
    listFileNames(pathToSampleFolder(category))


  def listFileNames(dir: String): Set[String] = ls(dir).filter(_.isFile).map(_.getName).toSet

  def pathToFolder(fbID: String, cardID: String): String =
    absolutePathToData + s"users/$fbID/$cardID/"

  def pathToImage(fbID: String, cardID: String, fileName: String): String =
    absolutePathToData + s"users/$fbID/$cardID/$fileName"

  def pathToValidationFolder(category: String): String =
    s"$validationDir$category/"

  def pathToValidationImage(category: String, fileName: String): String =
    pathToValidationFolder(category) + fileName

  def pathToSampleFolder(category: String): String =
    s"$sampleDir$category/"

  def pathToSampleImage(category: String, fileName: String): String =
    pathToSampleFolder(category) + fileName

}
