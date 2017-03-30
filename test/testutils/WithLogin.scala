package testutils

import controllers.Fake

abstract class WithLogin(id: String) extends WithApp {

  override lazy val conf: Seq[(String, Any)] = Seq("mongo.test.id" -> id)

  lazy val (loggedUser, loggedCookie) = Fake.userAndCookie(0)(app)
  lazy val (loggedUser2, loggedCookie2) = Fake.userAndCookie(1)(app)
}
