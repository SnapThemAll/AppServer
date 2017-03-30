package models.services

import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorRef
import com.google.inject.name.Named
import jobs.TrackAnalysisManager.Analyze
import models.analysis.{Parameters, TimePredictor}
import models.daos.TrackDAO
import models.{TrackData, TrackFilter}
import utils.xml.XMLWriter

import scala.concurrent.Future
import scala.util.Success
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Handles actions to tracks.
  *
  * @param trackDAO The track DAO implementation.
  */
class TrackServiceImpl @Inject()(trackDAO: TrackDAO,
                                 @Named("track-analysis-manager-actor") trackAnalysisManager: ActorRef)
    extends TrackService {

  private[this] def maybeAnalyze(t: Track): Unit = {
    if (t.analyses.isEmpty) {
      trackAnalysisManager ! Analyze(t)
    }
  }

  override def retrieve(userID: UUID, trackID: UUID) = verifyTrackOwnership(userID, trackID) { t =>
    maybeAnalyze(t)
    t.toTrackData
  }

  override def retrieveAnalysis(userID: UUID, trackID: UUID) =
    verifyTrackOwnership(userID, trackID) { t =>
      XMLWriter.analysisToXML(t.analyses).toString
    }

  override def retrieveAll(userID: UUID) = trackDAO.findAll(userID).map(_.map(_.trackID))

  override def retrievePredicted(parameters: TrackFilter): Future[String] = {
    val defaultOldTracksQuantity = 10
    val futureOldTracks    = trackDAO.findAll(parameters.userID, defaultOldTracksQuantity)
    val futureSharedTracks = trackDAO.findShared(parameters)
    val futureWithPrediction = for {
      tracks    <- futureSharedTracks
      oldTracks <- futureOldTracks
    } yield {
      for {
        track <- tracks
      } yield {
        track -> TimePredictor(track, Parameters()).computePrediction(oldTracks)
      }
    }
    futureWithPrediction.map(XMLWriter.tracksWithPredictionsToXML(_).toString)
  }

  override def save(userID: UUID, trackData: TrackData) = {
    val track = trackData.toTrack(userID)
    trackDAO
      .save(track)
      .andThen {
        case Success(t) => trackAnalysisManager ! Analyze(t)
      }
      .map(_.trackID)
  }

  override def remove(userID: UUID, trackID: UUID) = trackDAO.remove(trackID, userID).map(_ => trackID)

  private def verifyTrackOwnership[T](userID: UUID, trackID: UUID)(whatToDo: => (Track => T)): Future[T] =
    trackDAO.find(trackID).map {
      case Some(track) if track.userID == userID => whatToDo(track)
      case Some(_)                               => throw new IllegalAccessException("Accessing somebody else's track is illegal")
      case None                                  => throw new NoSuchElementException("No track with this trackID found")
    }

}
