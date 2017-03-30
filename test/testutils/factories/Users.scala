package testutils.factories

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.util.Random

/**
  * A factory for generating users for testing purposes
  */
object Users {

  /**
    * Generates a random user for testing purposes. Most values are picked at random.
    * @return a fresh random user
    */
  def random: User = {
    def randomAlphanumeric(i: Int) = {
      val rdmChar = Random.alphanumeric
      (1 to i).map(rdmChar).mkString
    }
    val fakeFirstName = randomAlphanumeric(6)
    val fakeName      = randomAlphanumeric(8)
    val fakeEmail     = fakeFirstName + "." + fakeName + "@fake.fake"
    User(
      UUID.randomUUID(),
      LoginInfo("fake", fakeEmail),
      Some(fakeFirstName),
      Some(fakeName),
      Some(fakeEmail),
      None,
      true
    )
  }
}
