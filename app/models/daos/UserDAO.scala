package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

/**
  * Give access to the user object.
  */
trait UserDAO {

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
    * Finds a user by its user ID.
    *
    * @param userID The ID of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  def find(userID: UUID): Future[Option[User]]

  def findAll(): Future[Seq[User]]

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: User): Future[User]

  /**
    * Removes the user for the given ID.
    *
    * @param userID The ID for which the user should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(userID: UUID): Future[Unit]

  /**
    * Removes the user for the login info.
    *
    * @param loginInfo The login info for which the user should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(loginInfo: LoginInfo): Future[Unit]

  /**
    * Deletes everything from the users collection. Use with caution !
    * @return A future to wait for the process to be completed.
    */
  private[daos] def dropAll: Future[Unit]

}
