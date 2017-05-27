package models.daos.mongo

import com.google.inject.{Inject, Singleton}
import models.Message
import models.daos.MessageDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._

import scala.concurrent.Future


/**
  * Give access to the [[Message]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
@Singleton
class MessageDAOMongo @Inject()(mongoDB: Mongo) extends MessageDAO {

  implicit val updateJsonFormat: OFormat[Message] = Message.jsonFormat

  private[this] def messageColl = mongoDB.collection("message")

  override def save(message: Message): Future[Message] = {
    messageColl
      .flatMap(_.update(Json.obj("_id" -> "default"), message, upsert = true))
      .transform(
        _ => message,
        t => t
      )
  }

  override def find: Future[Option[Message]] = {
    messageColl.flatMap(
      _.find(Json.obj("_id" -> "default")).one[Message]
    )
  }
}

