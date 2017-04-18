package models.daos

import models.Card

import scala.concurrent.Future

trait CardDAO {

  def save(card: Card): Future[Card]

  def savePicture(fbID: String, cardName: String, fileName: String): Future[Double]

  def find(fbID: String, cardName: String): Future[Option[Card]]

  def findAll(fbID: String): Future[IndexedSeq[Card]]

  def remove(fbID: String, cardName: String): Future[Unit]

  def removePicture(fbID: String, cardName: String, fileName: String): Future[Option[Card]]

}
