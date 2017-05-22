package models.services

import models.Feedback

import scala.concurrent.Future

/**
  * Handles actions to cards.
  */
trait FeedbackService {

  def save(message: String, fbID: String): Future[Feedback]

}
