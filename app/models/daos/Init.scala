package models.daos

import com.google.inject.{Inject, Singleton}
import models.{Category, UserCategory, ValidationCategory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

@Singleton
class Init @Inject() (validationCategoryDAO: ValidationCategoryDAO) {

  println("Checking if init is needed")

  validationCategoryDAO.find("backpack").flatMap{ catOpt =>
    if(catOpt.isDefined){
      Future.successful(println("Database is already setup"))
    } else {
      println("Setting up database")
      setupValidationCategories(validationCategoryDAO)
      .map(_ => println("Database updated"))
    }

  }
//
//
//  private def setupValidationCategories2(validationCategoryDAO: ValidationCategoryDAO): Future[Unit] = {
//    import utils.DataVariables.{categories, computeSampleCategory, computeValidationCategory}
//
//    categories.foreach{ catName =>
//      val validationCategory = computeValidationCategory(catName)
//      val sampleCategory = computeSampleCategory(catName)
//    }
//  }

  private def setupValidationCategories(validationCategoryDAO: ValidationCategoryDAO): Future[Stream[ValidationCategory]] = {
    import utils.DataVariables._

    validationCategories // STARTS THE COMPUTATIONS (LAZY VAL), Hence, (as it is blocking)
    sampleCategories     // we're (almost) sure DB will be ready after the startup

    val (beforeUserSet, fakeUserSet) = partitionCategories(1f/4, sampleCategories)

    initValidationCategories(validationCategories)
      .flatMap{ validationCat =>
        Future.sequence(
          addUserSet(beforeUserSet, validationCat).map( valCat =>
            validationCategoryDAO.save(valCat.copy( averageGain = 0f, numberOfImprovements = 0l))
          )
        )
      }
      .flatMap{ validationCat =>
        Future.sequence(
          addUserSet(fakeUserSet, validationCat).map(validationCategoryDAO.save)
        )
      }
  }


  private def addUserSet(userSet: Stream[Category], validationSet: Stream[ValidationCategory]): Stream[ValidationCategory] = {
    validationSet.map { valCat =>
      val userCat = userSet.find(_.name == valCat.name)
        .getOrElse(throw new IllegalArgumentException
        (s"Both set should contain the same categories (${valCat.name} is missing in beforeUserSet"))

      valCat.addFP(userCat.picturesFP)
    }
  }


  private def initValidationCategories(validationSet: Stream[Category]): Future[Stream[ValidationCategory]] = {
    Future.sequence(
      validationSet.map{cat =>
        validationCategoryDAO.save(ValidationCategory.initFromCategory(cat))
      }
    )
  }

  private def partitionCategories(percentage: Float, categories: Stream[Category]): (Stream[Category], Stream[Category]) = {
    require(percentage >= 0 && percentage <= 1, s"percentage shoud be between 0 and 1, not $percentage")

    categories.map{ cat =>
      val splitIndex = Math.round(cat.picturesFP.size * percentage)
      val (left, right) = cat.picturesFP.splitAt(splitIndex)
      UserCategory(cat.name, left) ->
        UserCategory(cat.name, right)
    }.unzip
  }

}