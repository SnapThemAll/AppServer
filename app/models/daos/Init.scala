package models.daos

import com.google.inject.{Inject, Singleton}
import models.{Descriptor, ValidationCategory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.Logger

import scala.concurrent.Future
import utils.DataVariables.categories

@Singleton
class Init @Inject() (validationCategoryDAO: ValidationCategoryDAO) extends Logger {

  log("Checking if init is needed")

  Future.sequence{
    categories
      .map { category =>
        validationCategoryDAO.find(category)
          .map(optCategory => category -> optCategory.isEmpty)
      }
  } .map(_.filter(_._2))
    .map(_.map(_._1))
    .foreach{ categoriesToCompute =>
      if(categoriesToCompute.isEmpty){
        Future.successful(log("Database is already setup"))
      } else {
        log("Updating database")
        setupValidationCategories(categoriesToCompute, validationCategoryDAO)
          .map(numCategoriesUpdated => log(s"Database updated (with $numCategoriesUpdated new categories)"))
      }

    }

  private def percentage(percent: Int): Float = percent / 100f


  private def setupValidationCategories(categoriesToCompute: Set[String], validationCategoryDAO: ValidationCategoryDAO): Future[Int] = {
    import utils.DataVariables.{listValidationFileNames, listSampleFileNames, pathToSampleImage}

    Future.sequence {
      categoriesToCompute.toList.map{ catName =>
        log(s"Creating validation category $catName...")
        var validationCategory = ValidationCategory.initFromCategory(catName, listValidationFileNames(catName))
        log("DONE")
        log(s"Computing sample descriptors of category $catName...")
        val sampleDescriptors = listSampleFileNames(catName).map(fileName => Descriptor.fromImagePath(pathToSampleImage(catName, fileName)))
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
    }.map(_.size)
  }

}