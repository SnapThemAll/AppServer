package computing

import java.io.File


object ComputingUtils {

  lazy val validationSet: Set[Category] = dataSetFromFolder("data/validation/")

  lazy val userSet = dataSetFromFolder("data/user/")

  def getListOfFileNames(dir: String): List[String] = {
    getListOfFiles(dir).filter(_.isFile).map(_.getName)
  }
  def getListOfFolderNames(dir: String): List[String] = {
    getListOfFiles(dir).filter(_.isDirectory).map(_.getName)
  }

  private def dataSetFromFolder(dir: String) =
    getListOfFolderNames(dir)
      .map{folderName =>
        val dataSet = getListOfFileNames(dir + folderName)
          .map(PictureFingerPrint.fromImagePath)
          .toSet
        Category(folderName, dataSet)
      }.toSet

  private def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.toList
    } else {
      Nil
    }

  }
}
