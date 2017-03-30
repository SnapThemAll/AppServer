package jobs

import java.util.UUID

import models.AuthToken
import models.daos.AuthTokenDAO
import org.joda.time.{DateTime, DateTimeZone}
import testutils.WithDAO

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import org.scalatestplus.play.PlaySpec

class SchedulerTest extends PlaySpec with FutureAwaits with DefaultAwaitTimeout {

  val totalExpiredTokens = 5
  val expiredTokens = Vector.tabulate(totalExpiredTokens)(
      n =>
        AuthToken(UUID.randomUUID(),
                  UUID.randomUUID(),
                  DateTime.now(DateTimeZone.getDefault).minusSeconds((n + 1) * 3600)))

  val cleanerInterval = "jobs.tokencleaner.interval" -> "5 seconds"

  "Scheduler" should {
    "effectively schedule cleaning task" in new WithDAO[AuthTokenDAO]("schedule_cleaning", cleanerInterval) {
      val addOps = expiredTokens map dao.save
      addOps.map(await(_))

      Thread.sleep(6000)
      val expiredRetrieved = await(dao.findExpired(DateTime.now(DateTimeZone.getDefault)))
      expiredRetrieved must have length 0 //They have been cleaned, shouldn't be in the DAO anymore
    }
    "be able to cancel scheduled task" in new WithDAO[AuthTokenDAO]("cancel_schedule", cleanerInterval) {
      val scheduler = inject[Scheduler]

      scheduler.scheduleCleaner.cancel()
      scheduler.scheduleCleaner.isCancelled mustBe true

      Thread.sleep(6000) //Wait in case the scheduler was already performing its last job

      val addOps = expiredTokens map dao.save
      addOps.map(await(_))

      val expiredRetrieved = await(dao.findExpired(DateTime.now(DateTimeZone.getDefault)))
      expiredRetrieved must have length expiredTokens.length //They should not have been cleaned
    }
  }
}
