package models.services

import java.util.UUID

import models.{TrackData, TrackFilter}

import scala.concurrent.Future

/**
  * Handles actions to tracks.
  */
trait TrackService {

  /**
    * Retrieves a track that matches the specified ID.
    *
    * @param userID The user willing to retrieve his track
    * @param trackID The ID to retrieve a track.
    * @return The retrieved track or None if no user could be retrieved for the given ID.
    */
  def retrieve(userID: UUID, trackID: UUID): Future[TrackData]

  /**
    * Retrieves the analysis of atrack that matches the specified ID.
    *
    * @param userID The user willing to retrieve his track analysis
    * @param trackID The ID of the track
    * @return The analysis of the track as a XML String
    */
  def retrieveAnalysis(userID: UUID, trackID: UUID): Future[String]

  /**
    * Retrieves all tracks of a user.
    *
    * @param userID The ID of the user.
    * @return The id of the tracks of the user.
    */
  def retrieveAll(userID: UUID): Future[Seq[UUID]]

  /**
    * Retrieves some tracks not owned by this user with a predicted time based on the user's overall performance
    * on his own tracks
    *
    * @param parameters The parameters used to fine-tune the retrieved predictions
    * @return the list of the predicted tracks with their respective time in seconds
    */
  def retrievePredicted(parameters: TrackFilter): Future[String]

  /**
    * Saves a track.
    *
    * @param userID The user willing to save his track
    * @param trackData The track to save.
    * @return The saved track.
    */
  def save(userID: UUID, trackData: TrackData): Future[UUID]

  /**
    * Removes a track of a user.
    * @param userID The user willing to remove his track.
    * @param trackID  The track to remove.
    * @return the trackID of the deleted track
    */
  def remove(userID: UUID, trackID: UUID): Future[UUID]
}
