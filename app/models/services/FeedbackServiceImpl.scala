package models.services

import com.google.inject.Inject
import models.Feedback
import models.daos.FeedbackDAO

import scala.concurrent.Future

class FeedbackServiceImpl @Inject()(feedbackDAO: FeedbackDAO) extends FeedbackService {

  override def save(message: String, fbID: String): Future[Feedback] =
    feedbackDAO.save(Feedback(message, fbID))

}
