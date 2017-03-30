package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import testutils.WithDAO

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class UserDAOTest extends PlaySpec with FutureAwaits with DefaultAwaitTimeout {

  def getUser = User(
      UUID.randomUUID(),
      LoginInfo("a", "b"),
      Some("Jean"),
      Some("Dupont"),
      Some("mail@example.com"),
      Some("url.example.com"),
      true
  )

  val user = getUser
  val user2 = getUser

  "A UserDAO" should {

    "save a user" in new WithDAO[UserDAO]("save_user") {
      val userAdded = await(dao.save(user))
      userAdded shouldBe user
    }
    "retrieves added user using userID" in new WithDAO[UserDAO]("save_and_retrieve_user_with_id") {
      await(dao.save(user))
      val userFound = await(dao.find(user.userID))
      userFound shouldBe defined
      userFound.get shouldBe user
    }
    "retrieves added user using login info" in new WithDAO[UserDAO]("save_and_retrieve_user_with_loginInfo") {
      await(dao.save(user))
      val userFound = await(dao.find(user.loginInfo))
      userFound shouldBe defined
      userFound.get shouldBe user
    }
    "not retrieves a removed user (using userID)" in new WithDAO[UserDAO]("save_remove_retrieve_user_with_id") {
      await(dao.save(user))
      await(dao.remove(user.userID))
      val userFound = await(dao.find(user.userID))
      userFound shouldBe None
    }
    "not retrieves a removed user (using loginInfo)" in new WithDAO[UserDAO]("save_remove_retrieve_user_with_id") {
      await(dao.save(user))
      await(dao.remove(user.loginInfo))
      val userFound = await(dao.find(user.loginInfo))
      userFound shouldBe None
    }
    "drop all users if dropAll is called" in new WithDAO[UserDAO]("dropAll_remove_all_users") {
      await(dao.save(user))
      await(dao.save(user2))
      await(dao.dropAll)
      val userFound = await(dao.find(user.userID))
      val userFound2 = await(dao.find(user2.loginInfo))
      userFound shouldBe None
      userFound2 shouldBe None
    }
  }

}
