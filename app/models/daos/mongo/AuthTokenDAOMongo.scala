package models.daos.mongo

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import models.AuthToken
import models.AuthToken.jsonFormat
import models.daos.AuthTokenDAO
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.play.json._

import scala.concurrent.Future

/**
  * Give access to the [[AuthToken]] object.
  * Uses quill to access the database
  */
@Singleton
class AuthTokenDAOMongo @Inject()(mongoDB: Mongo) extends AuthTokenDAO {

  private[this] def authTokenColl = mongoDB.collection("authtoken")

  /**
    * Finds a token by its ID.
    *
    * @param _id The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  override def find(_id: UUID): Future[Option[AuthToken]] = authTokenColl.flatMap(
      _.find(Json.obj("_id" -> _id)).one[AuthToken]
  )

  /**
    * Finds expired tokens.
    *
    * @param dateTime The current date time.
    */
  override def findExpired(dateTime: DateTime): Future[Seq[AuthToken]] = {
    authTokenColl.flatMap(
        _.find(Json.obj("expiry" -> Json.obj("$lt" -> dateTime)))
          .cursor[AuthToken]()
          .collect[Seq](-1, Mongo.cursonErrorHandler[AuthToken]("findExpired in auth token dao"))
    )
  }

  /**
    * Saves a token.
    *
    * @param token The token to save.
    * @return The saved token.
    */
  override def save(token: AuthToken): Future[AuthToken] =
    authTokenColl
      .flatMap(_.insert[AuthToken](token))
      .transform(
          _ => token,
          t => t
      )

  /**
    * Removes the token for the given ID.
    *
    * @param _id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
  override def remove(_id: UUID): Future[Unit] =
    authTokenColl
      .flatMap(_.remove(Json.obj("_id" -> _id)))
      .transform(
          _ => (),
          t => t
      )

  override def dropAll =
    authTokenColl
      .flatMap(_.remove(Json.obj()))
      .transform(
          _ => (),
          t => t
      )
}
