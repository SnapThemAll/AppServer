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
      setupValidationCategories2(validationCategoryDAO)
      .map(numCat => log(s"Database set up (with $numCat categories)"))
    }

  }

  private def percentage(percent: Int): Float = percent / 100f


  private def setupValidationCategories2(validationCategoryDAO: ValidationCategoryDAO): Future[Int] = {
    import utils.DataVariables._

    Future.sequence {
      categories.map { catName =>
        var validationCategory = ValidationCategory.initFromCategory(catName, listValidationFileNames(catName))
        val sampleDescriptors = listSampleFileNames(catName).map(Descriptor.fromImagePath)

        val splitIndex = Math.round(sampleDescriptors.size * percentage(65) )
        val (beforeUserDescriptors, fakeUserDescriptors) = sampleDescriptors.splitAt(splitIndex)

        validationCategory = validationCategory
          .computeSimilarities(beforeUserDescriptors)
          .copy(averageGain = 0f, numberOfImprovements = 0)

        validationCategory = validationCategory
          .computeSimilarities(fakeUserDescriptors)

        validationCategoryDAO.save(validationCategory).map(_ => true)
      }
    }.map(_.count(_ && true))
  }

}