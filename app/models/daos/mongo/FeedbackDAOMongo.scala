package models.daos.mongo

import com.google.inject.{Inject, Singleton}
import models.Feedback
import models.daos.FeedbackDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._

import scala.concurrent.Future


/**
  * Give access to the [[Feedback]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
@Singleton
class FeedbackDAOMongo @Inject()(mongoDB: Mongo) extends FeedbackDAO {

  implicit val feedbackJsonFormat: OFormat[Feedback] = Feedback.jsonFormat

  private[this] def feedbackColl = mongoDB.collection("feedback")

  override def save(feedback: Feedback): Future[Feedback] = {
    feedbackColl
      .flatMap(_.update(Json.obj("_id" -> feedback._id, "date" -> feedback.date), feedback, upsert = true))
      .transform(
        _ => feedback,
        t => t
      )
  }

  override def findAll(fbID: String): Future[Seq[Feedback]] =
    feedbackColl.flatMap(
      _.find(Json.obj("fbID" -> fbID))
        .cursor[Feedback]()
        .collect[Seq](-1, Mongo.cursonErrorHandler[Feedback]("findAll in feedback dao"))
    )
}

