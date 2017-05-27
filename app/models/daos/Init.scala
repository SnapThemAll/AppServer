package models.daos

import com.google.inject.{Inject, Singleton}
import models.{Descriptor, Message, Update, ValidationCategory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.DataVariables.{cacheIsFull, categories, computeClutterDescriptors, getValidationDescriptors}
import utils.Logger

import scala.concurrent.Future

@Singleton
class Init @Inject() (messageDAO: MessageDAO, validationCategoryDAO: ValidationCategoryDAO) extends Logger {

  log("Init starting...")

  Future.sequence{
    categories
      .map { category =>
        validationCategoryDAO.find(category)
          .map(optCategory => category -> optCategory.isEmpty)
      }
  } .map(_.filter(_._2))
    .map(_.map(_._1))
    .map{ categoriesToCompute =>
      if(categoriesToCompute.isEmpty){
        Future.successful {
          log("Database is already setup")
        }
      } else {
        log("Updating database")
        setupValidationCategories(categoriesToCompute, validationCategoryDAO)
          .map(numCategoriesUpdated => log(s"Database updated (with $numCategoriesUpdated new categories)"))
      }
    }
    .foreach{_ =>
      categories.foreach( category =>
        if(!cacheIsFull){
          getValidationDescriptors(category)
        }
      )
    }

  private def percentage(percent: Int): Float = percent / 100f

  private def setupValidationCategories(categoriesToCompute: Set[String], validationCategoryDAO: ValidationCategoryDAO): Future[Int] = {
    import utils.DataVariables.{listSampleFileNames, listValidationFileNames, pathToSampleImage}

    log(s"Computing clutter descriptors...")
    val (fewClutter, mostClutter) = computeClutterDescriptors.splitAt(20)
    log(s"DONE")

    Future.sequence {
      categoriesToCompute.toList.map{ catName =>
        log(s"Creating validation category $catName...")
        var validationCategory = ValidationCategory.initFromCategory(catName, listValidationFileNames(catName))
        log("DONE")

        log(s"Computing sample descriptors of category $catName...")
        val sampleDescriptors = listSampleFileNames(catName).map(fileName => Descriptor.fromImagePath(pathToSampleImage(catName, fileName)))
        log("DONE")


        log(s"Adding most clutter descriptors to validation category $catName")
        validationCategory = validationCategory
          .computeSimilarities(mostClutter)
          .copy(averageGain = 0f, numberOfImprovements = 0)
        log("DONE")

        log(s"Adding few clutter descriptors to validation category $catName")
        validationCategory = validationCategory
          .computeSimilarities(fewClutter)
        log("DONE")

        log(s"Adding sample descriptors to validation category $catName")
        validationCategory = validationCategory
          .computeSimilarities(sampleDescriptors)
        log("DONE")

        validationCategoryDAO.save(validationCategory)
          .map(_ => true)
      }
    }.map(_.size)
  }

}