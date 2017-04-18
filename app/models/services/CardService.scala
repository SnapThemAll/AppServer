package models.services

import models.{Card, Picture}

import scala.concurrent.Future

/**
  * Handles actions to cards.
  */
trait CardService {

  def savePicture(fbID: String, cardName: String, fileName: String): Future[Double]

  def retrieve(fbID: String, cardName: String): Future[Option[Card]]

  def retrievePicture(fbID: String, cardName: String, fileName: String): Future[Option[Picture]]

  def retrieveAll(fbID: String): Future[IndexedSeq[Card]]

  def computeTotalScore(fbID: String): Future[Double]

  def removePicture(fbID: String, cardName: String, fileName: String): Future[Option[Card]]
}
