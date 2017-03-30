package models.daos

import java.util.UUID

import models.TrackFilter
import models.geometry.Track

import scala.concurrent.Future

trait TrackDAO {

  /**
    * Finds a track by its ID.
    *
    * @param trackID The ID of the track to find.
    * @return The found track or None if no track for the given ID could be found.
    */
  def find(trackID: UUID): Future[Option[Track]]

  /**
    * Finds some tracks shared by other users based on some additional filters
    *
    * @return A future sequence of tracks
    */
  def findShared(parameters: TrackFilter): Future[Seq[Track]]

  /**
    * Finds up to "max" tracks shared by users other than the one specified by "userID"
    *
    * @param userID The ID of the user willing to get the shared tracks (to give him tracks that doesn't belong to him)
    * @param max The maximum number of tracks to get back (will get max if there's enough tracks)
    * @return A future sequence of tracks
    */
  def findShared(userID: UUID, max: Int): Future[Seq[Track]] =
    findShared(TrackFilter(userID = userID, maxLength = max))

  /**
    * Finds all tracks of a user up to "limit".
    *
    * @param userID The ID of the user.
    * @param limit The maximum number of tracks to return
    * @return The found tracks of the user.
    */
  def findAll(userID: UUID, limit: Int = -1): Future[Seq[Track]]

  /**
    * Saves a track.
    *
    * @param track The track to save.
    * @return The saved track.
    */
  def save(track: Track): Future[Track]

  /**
    * Removes the track for the given ID.
    *
    * @param trackID The ID of the track to remove.
    * @param userID The ID of the user whose the track belong to
    * @return A future to wait for the process to be completed.
    */
  def remove(trackID: UUID, userID: UUID): Future[Unit]

}
