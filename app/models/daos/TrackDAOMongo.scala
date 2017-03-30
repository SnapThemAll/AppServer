package models.daos

import java.util.UUID

import com.google.inject.Inject
import models.TrackFilter
import models.geometry.{Location, Track}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

/**
  * Give access to the [[Track]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
class TrackDAOMongo @Inject()(mongoDB: Mongo) extends TrackDAO {

  private[this] def trackColl = mongoDB.collection("track")

  trackColl.map(_.indexesManager.ensure(Index(Seq("loc" -> IndexType.Geo2DSpherical))))

  override def find(trackID: UUID): Future[Option[Track]] =
    trackColl.flatMap(_.find(Json.obj("trackID" -> trackID)).one[Track])

  override def findShared(filter: TrackFilter): Future[Seq[Track]] = {
    trackColl.flatMap(
        _.find(
            Json.obj(
                "userID" -> Json.obj("$ne"   -> filter.userID),
                "loc"    -> Json.obj("$near" -> Json.obj("$geometry" -> Location(filter.nearLat, filter.nearLng)))
            )).cursor[Track]().collect[Seq](filter.amount, Mongo.cursonErrorHandler[Track]("findShared in track dao")))
  }

  override def findShared(userID: UUID, max: Int): Future[Seq[Track]] = {
    trackColl.flatMap(
        _.find(Json.obj("userID" -> Json.obj("$ne" -> userID)))
          .cursor[Track]()
          .collect[Seq](max, Mongo.cursonErrorHandler[Track]("findShared in track dao")))
  }

  override def findAll(userID: UUID, limit: Int = -1): Future[Seq[Track]] = {
    trackColl.flatMap(
        _.find(Json.obj("userID" -> userID))
          .cursor[Track]()
          .collect[Seq](limit, Mongo.cursonErrorHandler[Track]("findAll in track dao")))
  }

  override def save(track: Track): Future[Track] = {
    trackColl
      .flatMap(_.update(Json.obj("trackID" -> track.trackID), track, upsert = true))
      .transform(
          _ => track,
          t => t
      )
  }

  override def remove(trackID: UUID, userID: UUID): Future[Unit] =
    trackColl
      .flatMap(_.remove(Json.obj("trackID" -> trackID, "userID" -> userID)))
      .transform(
          _ => (),
          t => t
      )
}
