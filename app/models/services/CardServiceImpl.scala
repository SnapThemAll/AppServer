package models.services

import java.util.UUID
import javax.inject.Inject

import models.Card
import models.daos.CardDAO

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  */
class CardServiceImpl @Inject()(cardDAO: CardDAO) extends CardService {

  override def save(card: Card): Future[Card] = {
    cardDAO.save(card)
  }

  override def savePicture(userID: UUID, cardName: String, pictureURI: String): Future[Double] = {
    cardDAO.savePicture(userID, cardName, pictureURI).map(card => card.score)
  }

  override def retrieve(userID: UUID, cardName: String): Future[Option[Card]] = {
    cardDAO.find(userID, cardName)
  }

  override def retrievePicturesURI(userID: UUID, cardName: String): Future[IndexedSeq[String]] = {
    cardDAO.find(userID, cardName)
      .map{ maybeCard =>
        maybeCard.map(_.picturesURI).getOrElse(IndexedSeq.empty)
      }
  }

  override def retrieveAll(userID: UUID): Future[IndexedSeq[Card]] = {
    cardDAO.findAll(userID)
  }

  override def computeTotalScore(userID: UUID): Future[Double] = {
    cardDAO.findAll(userID)
      .map{ cards =>
        cards
          .groupBy( _.cardName )
          .map{ case(_, cardsByName) => cardsByName.map( _.score).max }
          .sum
      }
  }

  override def remove(userID: UUID, cardName: String): Future[Unit] = {
    cardDAO.remove(userID, cardName)
  }

  override def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]] = {
    cardDAO.removePicture(userID, cardName, pictureURI)
  }

}
