package computing

import models.{Picture, PictureFingerPrint, ValidationCategory}
import utils.{EnvironmentVariables, Files, Logger}

import scala.util.Random

object ScoreComputing extends Logger {

  import utils.EnvironmentVariables.validationSet

  def computeScore(newPictureFP: PictureFingerPrint, validationCategory: ValidationCategory) : (Float, ValidationCategory) = {

    val updatedValidationCategory = validationCategory.computeSimilarites(newPictureFP)
    val marginalGain = updatedValidationCategory.marginalGain(validationCategory)

    val score = marginalGain / updatedValidationCategory.averageGain // SCORE = GAIN / AVERAGE GAIN

    logger.info(s"Category ${validationCategory.name} improved by $marginalGain} (the bigger the better). Score: $score")

    score -> updatedValidationCategory
  }


  /*
  def w(picA: PictureFingerPrint, picB: PictureFingerPrint): Double = picA.similarityWith(picB)


  def fNNOfCategory(userCat: Category, validationCat: Category): Double = {
    require(userCat.name == validationCat.name,
      "fNN of a single category should be done the same one (user and validation)")

    (for{
      validationPic <- validationCat.pictures
    } yield {
      userCat.pictures
        .map(userPic => w(validationPic, userPic))
        .max
    }).sum
  }


  def fNN(userSet: Set[Category], validationSet: Set[Category]): Double = {
    require(userSet.size == validationSet.size,
      s"userSet size = ${userSet.size}, validationSet size = ${validationSet.size}. " +
        s"This means that they don't contain the same categories")
    userSet.map(_.name).foreach(catName =>
      require(validationSet.map(_.name).contains(catName),
        s"Category named $catName of the userSet is missing in the validationSet")
    )

    validationSet.map { y =>
      val userCat = userSet.find(cat => cat.name == y.name).get
      fNNOfCategory(userCat, y)
    }.sum
  }

  private def randomScore(from: Int, to: Int): Double = from + Random.nextDouble() * (to - from)

  private def randomClutter: PictureFingerPrint = {
    val clutterImages = Files.ls(EnvironmentVariables.absolutePathToData + "clutter")
    val randomClutter = clutterImages(Random.nextInt(clutterImages.size))
    PictureFingerPrint.fromImageFile(randomClutter)
  }

  private def fillSetWithClutter(set: Set[UserCategory], categories: Set[String]): Set[UserCategory] = {
    val setCatNames = set.map(_.name)
    categories.map{ catName =>
      set.find(cat => cat.name == catName).getOrElse(UserCategory(catName, Set(randomClutter)))
    }
  }
  */
}
