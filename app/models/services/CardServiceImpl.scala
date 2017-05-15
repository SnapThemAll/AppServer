package models.services

import javax.inject.Inject

import computing.{Category, ScoreComputing}
import models.{Card, Picture, PictureFingerPrint}
import models.daos.CardDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.EnvironmentVariables

import scala.concurrent.Future

/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  */
class CardServiceImpl @Inject()(cardDAO: CardDAO) extends CardService {

  override def savePicture(fbID: String, cardID: String, fileName: String): Future[Double] = {
    val newPicturePath = EnvironmentVariables.pathToImage(fbID, cardID, fileName)
    val newPictureFP = PictureFingerPrint.fromImagePath(newPicturePath)
    cardDAO.findAll(fbID)
      .flatMap{ cards =>
        val userSet = cards.map(Category.fromCard).toSet
        val score = ScoreComputing.computeScore(newPictureFP, cardID, userSet)
        cardDAO.savePicture(fbID, cardID, fileName, score)
      }
      .map(_.pictures.last.score)
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

  override def retrieveAll(fbID: String): Future[IndexedSeq[Card]] = {
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
