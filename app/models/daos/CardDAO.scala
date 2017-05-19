package models.daos

import models.{Card, Picture}

import scala.concurrent.Future

trait CardDAO {

  def save(card: Card): Future[Card]

  def savePicture(fbID: String, cardID: String, picture: Picture): Future[Card]

  def find(fbID: String, cardID: String): Future[Option[Card]]

  def findAll(fbID: String): Future[Seq[Card]]

  def findAllUsers(cardID: String): Future[Seq[Card]]

  def remove(fbID: String, cardID: String): Future[Unit]

  def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]]

}
