package models.daos.mongo

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.User.jsonFormat
import models.daos.UserDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.play.json._

import scala.concurrent.Future

/**
  * Give access to the [[User]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
@Singleton
class UserDAOMongo @Inject()(mongoDB: Mongo) extends UserDAO {

  private[this] def userColl = mongoDB.collection("user")

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  override def find(loginInfo: LoginInfo): Future[Option[User]] =
    userColl.flatMap(
      _.find(Json.obj("loginInfo" -> loginInfo)).one[User]
    )

  /**
    * Finds a user by its user ID.
    *
    * @param userID The ID of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  override def find(userID: UUID): Future[Option[User]] = userColl.flatMap(
    _.find(Json.obj("userID" -> userID)).one[User]
  )

  override def findAll(): Future[Seq[User]] =
    userColl.flatMap(
      _.find(Json.obj())
        .cursor[User]()
        .collect[Seq](-1, Mongo.cursonErrorHandler[User]("findAll in user dao"))
    )

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  override def save(user: User): Future[User] = {
    //If a user with the same loginInfo already existed (should be unique), we update it. Otherwise we insert
    userColl
      .flatMap(_.update(Json.obj("loginInfo" -> user.loginInfo), user, upsert = true))
      .transform(
        _ => user,
        t => t
      )
  }

  /**
    * Removes the user for the given ID.
    *
    * @param id The ID for which the token should be removed.
    * @return A future to wait for the process to be completed.
    */
  override def remove(id: UUID): Future[Unit] =
    userColl
      .flatMap(_.remove(Json.obj("userID" -> id)))
      .transform(
        _ => (),
        t => t
      )

  /**
    * Removes the user for the login info.
    *
    * @param loginInfo The login info for which the user should be removed.
    * @return A future to wait for the process to be completed.
    */
  override def remove(loginInfo: LoginInfo): Future[Unit] =
    userColl
      .flatMap(_.remove(Json.obj("loginInfo" -> loginInfo)))
      .transform(
        _ => (),
        t => t //TODO check the error and throw another more meaningfull error (e.g. NoSuchElement...)
      )

  private[daos] def dropAll =
    userColl
      .flatMap(_.remove(Json.obj()))
      .transform(
        _ => (),
        t => t
      )
}
