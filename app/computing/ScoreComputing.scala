package computing

import models.{Picture, PictureFingerPrint}
import utils.{EnvironmentVariables, Files}

import scala.util.Random

object ScoreComputing {

  import utils.EnvironmentVariables.validationSet

  def computeScore(newPictureFP: PictureFingerPrint, newPictureCategoryName: String, userSet: Set[Category]) : Double = {
    val userSetFilled = fillSetWithClutter(userSet, validationSet.map(_.name))
    val oldFNN = fNN(userSetFilled, validationSet)
    val newUserSet = userSetFilled.map{ cat =>
      if(cat.name == newPictureCategoryName) cat.addPictureFP(newPictureFP) else cat
    }
    val newFNN = fNN(newUserSet, validationSet)
    println("newFNN - oldFNN = " + newFNN - oldFNN)
    if(newFNN < oldFNN){
      5 + Random.nextDouble() * 5
    } else {
      Random.nextDouble() * 5
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
