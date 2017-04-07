package models.services

import java.util.UUID

import models.Card

import scala.concurrent.Future

/**
  * Handles actions to cards.
  */
trait CardService {

  def save(card: Card): Future[Card]

  def savePicture(userID: UUID, cardName: String, pictureURI: String): Future[Card]

  def retrieve(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]]

  def retrieveAll(userID: UUID): Future[IndexedSeq[Card]]

  def retrieveAllPicturesURI(userID: UUID, cardName: String): Future[IndexedSeq[String]]

  def computeTotalScore(userID: UUID): Future[Double]

  def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Unit]
}
