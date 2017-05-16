package models.services

import models.{Card, Picture, PictureFingerPrint}

import scala.concurrent.Future

/**
  * Handles actions to cards.
  */
trait CardService {

  def savePicture(fbID: String, cardID: String, fileName: String, fingerPrint: PictureFingerPrint): Future[Double]

  def retrieve(fbID: String, cardID: String): Future[Option[Card]]

  def retrievePicture(fbID: String, cardID: String, fileName: String): Future[Option[Picture]]

  def retrieveAll(fbID: String): Future[IndexedSeq[Card]]

  def computeTotalScore(fbID: String): Future[Double]

  def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]]
}
