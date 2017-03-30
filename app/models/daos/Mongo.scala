package models.daos

import com.google.inject.{Inject, Singleton}
import reactivemongo.api.{Cursor, DefaultDB, MongoConnection}
import reactivemongo.play.json.collection.JSONCollection
import utils.Logger

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Success, Try}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.{Configuration, Environment, Mode}

/**
  * Class that contains everything related to the access to the Mongo database. It's aware of its environment:
  * this class will load the correct database depending on if in prod, dev or testing mode. The configurations are
  * in application.conf
  *
  * @param environment the Play environment, holding (amongst other things) the mode the app is running in
  */
@Singleton
class Mongo @Inject()(environment: Environment, configuration: Configuration) extends Logger {
  import Mongo._

  /**
    * Returns a collection with the given name plus some eventual id defined in the configuration.
    * For example, if you pass the parameter "user" you could get back the collection whose name is "user_test1".
    * This id is in the configuration (key "mongo.somemode.collid") either hardcoded or requested by some test
    * that's running.
    *
    * @param name The name of the wished collection
    * @return future mongo collection with the given name to which is optionally appended some id
    */
  def collection(name: String): Future[JSONCollection] = db.map[JSONCollection](_.collection(name + collSuffix))

  private[this] lazy val config = configuration.underlying

  private[this] lazy val configPath: String = "mongo" + (environment.mode match {
    case Mode.Prod => ".prod"
    case Mode.Test => ".test"
    case Mode.Dev  => ""
  })

  private[this] def getStringConfig(name: String): Try[String] = {
    Try(config.getString(s"$configPath.$name"))
  }

  private[this] lazy val connection: MongoConnection = getStringConfig("server") match {
    case Success(server) =>
      logger.info("Established connection to Mongo server " + server)
      mongoDriver.connection(List(server))
    case _ =>
      logger.error(s"Can't find config for Mongo server under $configPath.server. Will use default server: localhost")
      mongoDriver.connection(List("localhost"))
  }

  private[this] val dbSuffix: String = getStringConfig("id") match {
    case Success(id) => "_" + id
    case _           => ""
  }

  private[this] val collSuffix: String = getStringConfig("collid") match {
    case Success(collid) => "_" + collid
    case _               => ""
  }

  /**
    * The actual Mongo database. You can access all the collections from it.
    *
    * @return future mongo database.
    */
  private[this] def db: Future[DefaultDB] = getStringConfig("database") match {
    case Success(database) =>
      connection.database(database + dbSuffix)
    case _ =>
      logger.error(
          s"Can't find config for Mongo database name under $configPath.database. Will use default db: steepup")
      connection.database("steepup" + dbSuffix)
  }

  //If we are in tests, start with a fresh DB
  if (environment.mode == Mode.Test) {
    Await.result(db.map(_.drop()), 5.seconds)
  }
}

object Mongo extends Logger {

  private lazy val mongoDriver = new reactivemongo.api.MongoDriver

  private[daos] def cursonErrorHandler[T](context: String = "a dao"): Cursor.ErrorHandler[Seq[T]] = {
    (last: Seq[T], error: Throwable) =>
      logger.error(s"Encounter error in $context: $error.\nLast: $last")

      if (last.isEmpty) { // continue, skip error if no previous value
        Cursor.Cont(last)
      } else {
        Cursor.Fail(error)
      }
  }

}
