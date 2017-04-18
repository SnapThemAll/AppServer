package models.services

import javax.inject.Inject

import models.{Card, Picture}
import models.daos.CardDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  */
class CardServiceImpl @Inject()(cardDAO: CardDAO) extends CardService {

  override def savePicture(fbID: String, cardName: String, fileName: String): Future[Double] = {
    cardDAO.savePicture(fbID, cardName, fileName)
  }

  override def retrieve(fbID: String, cardName: String): Future[Option[Card]] = {
    cardDAO.find(fbID, cardName).map{ maybeCard =>
      maybeCard.map(_.getNotDeleted).filter(_.pictures.nonEmpty)
    }
  }

  override def retrievePicture(fbID: String, cardName: String, fileName: String): Future[Option[Picture]] = {
    retrieve(fbID, cardName).map{ maybeCard =>
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
        cards
          .groupBy( _.cardName )
          .map{ case(_, cardsByName) => cardsByName.map( _.bestScore ).max }
          .foldLeft(0d)(_ + _)
      }
  }

  override def removePicture(fbID: String, cardName: String, fileName: String): Future[Option[Card]] = {
    cardDAO.removePicture(fbID, cardName, fileName)
  }
}
