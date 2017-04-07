package models.daos

import java.util.UUID

import models.Card

import scala.concurrent.Future

trait CardDAO {

  /**
    * Finds a card given of a user given its name and pictureURI.
    *
    * @param userID
    * @param cardName
    * @param pictureURI
    * @return
    */
  def find(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]]

  /**
    * Finds all picturesURI of a user given their name up to a certain "limit".
    *
    * @param userID
    * @param cardName
    * @return
    */
  def findAllPicturesURI(userID: UUID, cardName: String): Future[IndexedSeq[String]]

  /**
    * Finds all cards of a user up to a certain "limit".
    *
    * @param userID The ID of the user.
    * @return The found cards of the user.
    */
  def findAll(userID: UUID): Future[IndexedSeq[Card]]

  /**
    * Saves a card.
    *
    * @param card The card to save.
    * @return The saved track.
    */
  def save(card: Card): Future[Card]

  /**
    * Saves a picture URI.
    *
    * @param pictureURI The pictureURI to save.
    * @return The saved track.
    */
  def save(pictureURI: String): Future[String]

  /**
    *
    * @param userID
    * @param cardName
    * @param pictureURI
    * @return
    */
  def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Unit]

  /**
    *
    * @param userID
    * @param cardName
    * @return
    */
  def removeCard(userID: UUID, cardName: String): Future[Unit]

}
