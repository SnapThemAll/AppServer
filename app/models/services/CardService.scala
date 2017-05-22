package models.services

import models.{Card, Descriptor, Picture}

import scala.concurrent.Future

/**
  * Handles actions to cards.
  */
trait CardService {

  def savePicture(fbID: String, cardID: String, fileName: String, descriptor: Descriptor): Future[Double]

  def retrieve(fbID: String, cardID: String): Future[Option[Card]]

  def retrievePicture(fbID: String, cardID: String, fileName: String): Future[Option[Picture]]

  def retrieveAll(fbID: String): Future[Seq[Card]]

  def computeTotalScore(fbID: String): Future[Double]

  def computeTotalScoreAllUsers(): Future[Seq[(String, Double)]]

  def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]]
}
