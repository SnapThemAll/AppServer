package models.daos

import com.google.inject.{Inject, Singleton}
import models.{Descriptor, ValidationCategory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.Logger

import scala.concurrent.Future

@Singleton
class Init @Inject() (validationCategoryDAO: ValidationCategoryDAO) extends Logger {

  log("Checking if init is needed")

  validationCategoryDAO.find("keyboard").flatMap{ catOpt =>
    if(catOpt.isDefined){
      Future.successful(log("Database is already setup"))
    } else {
      log("Setting up database")
      setupValidationCategories(validationCategoryDAO)
      .map(numCat => log(s"Database set up (with $numCat categories)"))
    }

  }

  private def percentage(percent: Int): Float = percent / 100f


  private def setupValidationCategories(validationCategoryDAO: ValidationCategoryDAO): Future[Int] = {
    import utils.DataVariables._

    Future.sequence {
      categories.map { catName =>
        log(s"Creating validation category $catName...")
        var validationCategory = ValidationCategory.initFromCategory(catName, listValidationFileNames(catName))
        log("DONE")
        log(s"Computing sample descriptors of category $catName...")
        val sampleDescriptors = listSampleFileNames(catName).map(Descriptor.fromImagePath)
        log("DONE")

        val splitIndex = Math.round(sampleDescriptors.size * percentage(65) )
        val (beforeUserDescriptors, fakeUserDescriptors) = sampleDescriptors.splitAt(splitIndex)

        log(s"Adding before user descriptors to validation category $catName")
        validationCategory = validationCategory
          .computeSimilarities(beforeUserDescriptors)
          .copy(averageGain = 0f, numberOfImprovements = 0)
        log("DONE")

        log(s"Adding fake user descriptors to validation category $catName")
        validationCategory = validationCategory
          .computeSimilarities(fakeUserDescriptors)
        log("DONE")

        validationCategoryDAO.save(validationCategory).map(_ => true)
      }
    }.map(_.count(_ && true))
  }

}