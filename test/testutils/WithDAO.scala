package testutils

import java.util.UUID

import scala.reflect.ClassTag

/**
  * Provides a fresh Play application, an injector, a DAO and some useful methods. Particulary useful in tests: indeed,
  * this gives you a completely independent environment (and a separate database) to work in.<br>
  *
  * @param id some unique id to name the underlying database. Generally use the same id each time for the same test.
  *           Will use a random id if not provided
  * @tparam T The type of the DAO you want to be available in context. This will exist under the name "dao"
  */
abstract class WithDAO[T: ClassTag](id: String, config: (String, Any)*) extends WithApp {

  def this(conf: (String, Any)*) {
    this(UUID.randomUUID().toString, conf: _*)
  }

  def this() {
    this(UUID.randomUUID().toString)
  }

  override lazy val conf: Seq[(String, Any)] = ("mongo.test.id" -> id) +: config

  /**
    * A fresh DAO of the type specified as a parameter of this class
    */
  lazy val dao = inject[T]
}
