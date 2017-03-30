package jobs

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.AuthToken
import models.daos.AuthTokenDAO
import org.joda.time.{DateTime, DateTimeZone}
import testutils.WithDAO

import scala.concurrent.duration._
import scala.util.Random

import play.api.libs.concurrent.InjectedActorSupport
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import org.scalatestplus.play.PlaySpec

class AuthTokenCleanerTest extends PlaySpec with InjectedActorSupport with FutureAwaits with DefaultAwaitTimeout {
  import AuthTokenCleaner._

  implicit val timeout: Timeout = 15.seconds

  val totalExpiredTokens = 5
  val totalValidTokens   = 5

  val expiredTokens = Vector.tabulate(totalExpiredTokens)(n =>
    AuthToken(UUID.randomUUID(), UUID.randomUUID(), DateTime.now(DateTimeZone.getDefault).minusSeconds(n * 100)))
  val validTokens = Vector.tabulate(totalValidTokens)(n =>
    AuthToken(UUID.randomUUID(), UUID.randomUUID(), DateTime.now(DateTimeZone.getDefault).plusSeconds(100 + n * 10)))

  val furtherMostExpiry = validTokens.last.expiry.plus(1000) //An expiry date that will make all tokens look expired

  "Authentication tokens cleaner" should {

    "answer messages" in new WithDAO[AuthTokenDAO]("answer_messages") {
      val cleaner = injectNamed[ActorRef]("auth-token-cleaner-actor")

      val Done(futureCleaned) = await((cleaner ? Clean).mapTo[Done])
      val cleaned             = await(futureCleaned)
      cleaned must have length 0
    }
    "clean expired tokens" in new WithDAO[AuthTokenDAO]("clean_expired_tokens") {
      val cleaner = injectNamed[ActorRef]("auth-token-cleaner-actor")

      val addOps = expiredTokens map dao.save
      addOps.map(await(_))

      val Done(futureCleaned) = await((cleaner ? Clean).mapTo[Done])
      val cleaned             = await(futureCleaned)

      val expiredRetrieved = await(dao.findExpired(DateTime.now(DateTimeZone.getDefault)))
      expiredRetrieved must have length 0 //They have been cleaned, shouldn't be in the DAO anymore
    }
    "not clean valid tokens" in new WithDAO[AuthTokenDAO]("not_clean_valid_tokens") {
      val cleaner = injectNamed[ActorRef]("auth-token-cleaner-actor")

      val addOps = validTokens map dao.save
      addOps.map(await(_))

      val Done(futureCleaned) = await((cleaner ? Clean).mapTo[Done])
      val cleaned             = await(futureCleaned)

      val expiredRetrieved = await(dao.findExpired(DateTime.now(DateTimeZone.getDefault)))
      expiredRetrieved must have length 0

      val allRetrieved = await(dao.findExpired(furtherMostExpiry)) //Equivalent to finding all tokens
      //Is every valid token still there ?
      allRetrieved must have length validTokens.length
      validTokens foreach (allRetrieved must contain(_))
    }
    "clean expired while keeping valid tokens" in new WithDAO[AuthTokenDAO]("clean_expired_while_keeping_valid") {
      val cleaner = injectNamed[ActorRef]("auth-token-cleaner-actor")

      val allTokensShuffled = Random.shuffle(validTokens ++ expiredTokens)
      val addOps            = allTokensShuffled map dao.save
      addOps.map(await(_))

      val Done(futureCleaned) = await((cleaner ? Clean).mapTo[Done])
      val cleaned             = await(futureCleaned)

      val expiredRetrieved = await(dao.findExpired(DateTime.now(DateTimeZone.getDefault)))
      expiredRetrieved must have length 0 //They have been cleaned, shouldn't be in the DAO anymore

      val validRetrieved = await(dao.findExpired(furtherMostExpiry)) //Equivalent to finding all tokens
      //Is every valid token still there ?
      validRetrieved must have length validTokens.length
      validTokens foreach (validRetrieved must contain(_))
    }
  }

}
