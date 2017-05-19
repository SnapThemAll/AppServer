package models.services

import javax.inject.Inject

import computing.ScoreComputing
import models._
import models.daos.{CardDAO, ValidationCategoryDAO}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.EnvironmentVariables

import scala.concurrent.Future

/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  * @param validationCategoryDAO The validationCategory DAO implementation
  */
class CardServiceImpl @Inject()(cardDAO: CardDAO, validationCategoryDAO: ValidationCategoryDAO) extends CardService {

  override def savePicture(fbID: String, cardID: String, fileName: String, fingerPrint: PictureFingerPrint): Future[Double] = {
    validationCategoryDAO.find(cardID).map(_.get)
      .flatMap{ validationCategory =>
        
        val (score, newValidationCat) = ScoreComputing.computeScore(fingerPrint, validationCategory)
        val picture = Picture(fileName, score, fingerPrint)

        validationCategoryDAO.save(newValidationCat).flatMap{_ =>
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
