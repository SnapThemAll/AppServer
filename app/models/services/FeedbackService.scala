package models.services

import models.Feedback

import scala.concurrent.Future

trait FeedbackService {

  def save(message: String, fbID: String): Future[Feedback]

}
