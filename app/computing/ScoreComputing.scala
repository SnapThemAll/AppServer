package computing

import models.{Picture, PictureFingerPrint}
import utils.{EnvironmentVariables, Files}

import scala.util.Random

object ScoreComputing {

  import utils.EnvironmentVariables.validationSet

  def computeScore(newPictureFP: PictureFingerPrint, newPictureCategoryName: String, userSet: Set[Category]) : Double = {
    val userSetFilled = fillSetWithClutter(userSet, validationSet.map(_.name))
    val newUserSet = userSetFilled.map{ cat =>
      if(cat.name == newPictureCategoryName) cat.addPictureFP(newPictureFP) else cat
    }

    val oldFNN = fNN(userSetFilled, validationSet)
    val newFNN = fNN(newUserSet, validationSet)

    val dif = oldFNN - newFNN
    println(s"Category $newPictureCategoryName improved by $dif} " +
      s"(the bigger the better because oldFNN - newFNN) (newFNN = $newFNN)")
    if( dif > 1000){
      randomScore(5, 10)
    } else if (dif > 500) {
      randomScore(0, 5)
    } else {
      0d
    }
  }


  def w(picA: PictureFingerPrint, picB: PictureFingerPrint): Double = picA.distanceWith(picB)

  def fNN(userSet: Set[Category], validationSet: Set[Category]): Double = {
    require(userSet.size == validationSet.size,
      s"userSet size = ${userSet.size}, validationSet size = ${validationSet.size}. " +
        s"This means that they don't contain the same categories")
    userSet.map(_.name).foreach(catName =>
      require(validationSet.map(_.name).contains(catName),
        s"Category named $catName of the userSet is missing in the validationSet")
    )

    validationSet.map { y =>
      y.pictures.map{ i =>
        userSet.find(cat => cat.name == y.name)
          .get
          .pictures.map{ s =>
            w(i, s)
        }.min
      }.sum
    }.sum
  }

  private def randomScore(from: Int, to: Int): Double = from + Random.nextDouble() * (to - from)

  private def randomClutter: PictureFingerPrint = {
    val clutterImages = Files.ls(EnvironmentVariables.absolutePathToData + "clutter")
    val randomClutter = clutterImages(Random.nextInt(clutterImages.size))
    PictureFingerPrint.fromImageFile(randomClutter)
  }

  private def fillSetWithClutter(set: Set[Category], categories: Set[String]): Set[Category] = {
    val setCatNames = set.map(_.name)
    categories.map{ catName =>
      set.find(cat => cat.name == catName).getOrElse(Category(catName, Set(randomClutter)))
    }
  }
}
