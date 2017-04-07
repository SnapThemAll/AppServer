package models.daos

import java.util.UUID

import models.Card

import scala.concurrent.Future

trait CardDAO {

  def save(card: Card): Future[Card]

  def savePicture(userID: UUID, cardName: String, pictureURI: String): Future[Card]

  def find(userID: UUID, cardName: String): Future[Option[Card]]

  def findAll(userID: UUID): Future[IndexedSeq[Card]]

  def remove(userID: UUID, cardName: String): Future[Unit]

  def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]]

}
