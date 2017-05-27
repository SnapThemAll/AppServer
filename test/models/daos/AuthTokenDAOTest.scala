package models.daos

import java.util.UUID

import models.AuthToken
import org.joda.time.{DateTime, DateTimeZone}
import testutils.WithDAO

import scala.language.postfixOps

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class AuthTokenDAOTest extends PlaySpec with FutureAwaits with DefaultAwaitTimeout {

  def getValidAuthToken =
    AuthToken(UUID.randomUUID(), UUID.randomUUID(), DateTime.now(DateTimeZone.getDefault).plusSeconds(3600))
  def getExpiredAuthToken =
    AuthToken(UUID.randomUUID(), UUID.randomUUID(), DateTime.now(DateTimeZone.getDefault).minusSeconds(3600))
  def saveAuthToken(dao: AuthTokenDAO, authToken: AuthToken = getValidAuthToken): AuthToken = {
    await(dao.save(authToken))
    authToken
  }
  "A AuthTokenDAO" should {

    "save tokens" in new WithDAO[AuthTokenDAO]("auth_token_save") {
      val validAuthToken = getValidAuthToken
      val validAuthTokenAdded   = await(dao.save(validAuthToken))
      validAuthTokenAdded shouldBe validAuthToken
    }
    "retrieves a valid token" in new WithDAO[AuthTokenDAO]("auth_token_save_and_retrieve")  {
      val validAuthToken = saveAuthToken(dao)

      val tokenFound = await(dao.find(validAuthToken._id))
      tokenFound shouldBe defined
      tokenFound.get shouldBe validAuthToken
    }
    "retrieves expired token(s)" in new WithDAO[AuthTokenDAO]("auth_token_save_and_retrieve_expired") {
      val expiredAuthToken = saveAuthToken(dao, getExpiredAuthToken)

      val expiredTokenFound = await(dao.findExpired(DateTime.now(DateTimeZone.getDefault)))
      expiredTokenFound.size shouldBe 1
      expiredTokenFound.head shouldBe expiredAuthToken
    }
    "not retrieves removed token" in new WithDAO[AuthTokenDAO]("auth_token_save_and_try_retrieving_removed")  {
      val validAuthToken = saveAuthToken(dao)

      await(dao.remove(validAuthToken._id))
      val tokenFound = await(dao.find(validAuthToken._id))
      tokenFound shouldBe None
    }
    "not retrieves tokens if dropAll was called" in new WithDAO[AuthTokenDAO]("auth_token_save_and_try_retrieving_removed")  {
      val validAuthToken = saveAuthToken(dao)

      await(dao.dropAll)
      val tokenFound = await(dao.find(validAuthToken._id))
      tokenFound shouldBe None
    }
  }
}
