package models.services

import java.util.UUID
import javax.inject.Inject

import models.Card
import models.daos.CardDAO

import scala.concurrent.Future

/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  */
class CardServiceImpl @Inject()(cardDAO: CardDAO) extends CardService {

  override def save(card: Card): Future[Card] = {
    cardDAO.save(card)
  }

  override def retrieve(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]] = {
    cardDAO.find(userID, cardName, pictureURI)
  }

  override def retrieveAllPicturesURI(userID: UUID, cardName: String): Future[Seq[String]] = {
    cardDAO.findAllPicturesURI(userID, cardName)
  }

  override def retrieveAll(userID: UUID): Future[Seq[Card]] = {
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

  override def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Unit] = {
    cardDAO.removePicture(userID, cardName, pictureURI)
  }
}
