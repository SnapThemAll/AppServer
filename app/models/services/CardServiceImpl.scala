package models.services

import javax.inject.Inject

import computing.ScoreComputing
import models._
import models.daos.{CardDAO, ValidationCategoryDAO}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.{DataVariables, Logger}

import scala.concurrent.Future

/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  * @param validationCategoryDAO The validationCategory DAO implementation
  */
class CardServiceImpl @Inject()(cardDAO: CardDAO, validationCategoryDAO: ValidationCategoryDAO) extends CardService with Logger {

  override def savePicture(fbID: String, cardID: String, fileName: String, fingerPrint: PictureFingerPrint): Future[Double] = {
    log(s"Starts fetching category $cardID from the database...")
    validationCategoryDAO.find(cardID).map(_.get)
      .flatMap{ validationCategory =>

        log(s"Category $cardID fetched. Starts Computing the score...")
        val (score, newValidationCat) = ScoreComputing.computeScore(fingerPrint, validationCategory)
        val picture = Picture(fileName, score, fingerPrint)

        log(s"Score computed. Saving $cardID category in the database...")
        validationCategoryDAO.save(newValidationCat).flatMap{_ =>
          log(s"Category $cardID saved. Sending the score back.")
          cardDAO.savePicture(fbID, cardID, picture)
            .map(_ => score)
          }
        }
  }

  override def retrieve(fbID: String, cardID: String): Future[Option[Card]] = {
    cardDAO.find(fbID, cardID).map{ maybeCard =>
      maybeCard.map(_.getNotDeleted).filter(_.pictures.nonEmpty)
    }
  }

  override def retrievePicture(fbID: String, cardID: String, fileName: String): Future[Option[Picture]] = {
    retrieve(fbID, cardID).map{ maybeCard =>
      maybeCard.flatMap(_.pictures.find(_.fileName == fileName))
    }
  }

  override def retrieveAll(fbID: String): Future[Seq[Card]] = {
    cardDAO.findAll(fbID).map{ cards =>
      cards.map(_.getNotDeleted).filter(_.pictures.nonEmpty)
    }
  }

  override def computeTotalScore(fbID: String): Future[Double] = {
    retrieveAll(fbID)
      .map{ cards =>
        cards.map(_.bestScore).sum
      }
  }

  override def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]] = {
    cardDAO.removePicture(fbID, cardID, fileName)
  }
}
