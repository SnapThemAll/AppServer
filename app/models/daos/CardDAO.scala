package models.daos

import models.Card

import scala.concurrent.Future

trait CardDAO {

  def save(card: Card): Future[Card]

  def savePicture(fbID: String, cardID: String, fileName: String, score: Double): Future[Card]

  def find(fbID: String, cardID: String): Future[Option[Card]]

  def findAll(fbID: String): Future[IndexedSeq[Card]]

  def remove(fbID: String, cardID: String): Future[Unit]

  def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]]

}
