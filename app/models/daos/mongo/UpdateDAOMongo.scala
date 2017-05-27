package models.daos.mongo

import com.google.inject.{Inject, Singleton}
import models.Update
import models.daos.UpdateDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._

import scala.concurrent.Future


/**
  * Give access to the [[Update]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
@Singleton
class UpdateDAOMongo @Inject()(mongoDB: Mongo) extends UpdateDAO {

  implicit val updateJsonFormat: OFormat[Update] = Update.jsonFormat

  private[this] def updateColl = mongoDB.collection("update")

  override def save(update: Update): Future[Update] = {
    updateColl
      .flatMap(_.update(Json.obj("_id" -> "00000000-0000-0000-0000-000000000000"), update, upsert = true))
      .transform(
        _ => update,
        t => t
      )
  }

  override def find: Future[Option[Update]] = {
    updateColl.flatMap(
      _.find(Json.obj("_id" -> "00000000-0000-0000-0000-000000000000")).one[Update]
    )
  }
}

