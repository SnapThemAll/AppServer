package models.daos

import com.google.inject.Inject
import models.{Category, PictureFingerPrint, UserCategory, ValidationCategory}
import utils.EnvironmentVariables._
import utils.Files.ls

import scala.concurrent.Future

class Init @Inject() (validationCategoryDAO: ValidationCategoryDAO) {

  println("Checking if init is needed")

  validationCategoryDAO.find("backpack").flatMap{ catOpt =>
    if(catOpt.isDefined){
      println("Database is already setup")
      Future.successful()
    } else {
      println("Setting up database")
      initValidationCategories().map{ validationCat =>
        
      }
    }

  }

  private def initValidationCategories(): Future[Set[ValidationCategory]] = {
    Future.sequence(
      dataSetFromFolder(validationDir).map{cat =>
        validationCategoryDAO.save(
          ValidationCategory.initFromCategory(cat)
        )
      }
    )
  }

  private def dataSetFromFolder(dir: String): Set[Category] = {
    (for {
      cardFolder <- ls(dir).filter(_.isDirectory)
    } yield {
      val images = ls(cardFolder).filter(_.isFile)
      UserCategory(cardFolder.getName, images.map(PictureFingerPrint.fromImageFile).toSet)
    }).toSet
  }

}