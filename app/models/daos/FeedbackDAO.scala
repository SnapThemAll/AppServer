package models.daos

import models.Feedback

import scala.concurrent.Future

/**
  * Created by Greg on 19.05.2017.
  */
trait FeedbackDAO {

  def save(feedback: Feedback): Future[Feedback]

  def findAll(fbID: String): Future[Seq[Feedback]]

}
