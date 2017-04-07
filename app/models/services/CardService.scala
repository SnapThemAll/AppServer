package models.services

import java.util.UUID

import models.Card

import scala.concurrent.Future

/**
  * Handles actions to cards.
  */
trait CardService {

  def save(card: Card): Future[Card]

  def savePicture(userID: UUID, cardName: String, pictureURI: String): Future[Double]

  def retrieve(userID: UUID, cardName: String): Future[Option[Card]]

  def retrievePicturesURI(userID: UUID, cardName: String): Future[IndexedSeq[String]]

  def retrieveAll(userID: UUID): Future[IndexedSeq[Card]]

  def computeTotalScore(userID: UUID): Future[Double]

  def remove(userID: UUID, cardName: String): Future[Unit]

  def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]]
}
