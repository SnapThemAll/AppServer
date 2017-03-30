package jobs

import java.util.UUID

import akka.actor.SupervisorStrategy.{Decider, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, PoisonPill, SupervisorStrategy, Terminated}
import com.google.inject.Inject
import models.daos.TrackDAO
import models.geometry.Track

import scala.concurrent.{Future, Promise}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Dispatches the tracks analysis jobs and stores the results in the DAO. This is an actor, communicate with it using
  * messages.<br>
  * The supported operations are in the companion object
  */
class TrackAnalysisManager @Inject()(trackDAO: TrackDAO) extends Actor {

  import TrackAnalysisManager._
  import jobs.TrackAnalysisWorker._

  def receive: Receive = normal(Map(), Map())

  def normal(jobs: Map[ActorRef, (UUID, UUID)], analyses: Map[(UUID, UUID), Promise[Track]]): Receive = {
    case Analyze(track) =>
      analyses.get(track.trackID -> track.userID) match {
        case None => //The track is not already being analyze
          val promise = Promise[Track]
          val worker  = context.actorOf(TrackAnalysisWorker.props, name = s"track-analysis-worker-${analyses.size}")

          val id = track.trackID -> track.userID
          context.become(normal(jobs + (worker -> id), analyses + (id -> promise)))
          context.watch(worker)

          worker ! Analyze(track)
          sender ! Done(promise.future)
        case _ =>
          sender ! Pending(track)
      }

    case AnalysisDone(track) =>
      trackDAO.save(track)

    case WorkerDone(track) =>
      sender ! PoisonPill //Work finished, let's kill him !

      val id        = track.trackID -> track.userID
      val promise   = analyses(id)
      val insertion = trackDAO.save(track)
      insertion.map(promise.success)

      context.become(normal(jobs - sender, analyses - id))

    case Terminated(worker) =>
      jobs.get(worker) match {
        case Some(id) => //The worker crashed
          context.become(normal(jobs - worker, analyses - id))
          val promise = analyses(id)
          promise.failure(new Exception("Track analyzer crashed, analysis couldn't be completed"))

        case None => //We killed the worker before, do nothing for this message
      }
  }

  override final val supervisorStrategy: SupervisorStrategy = {
    def defaultDecider: Decider = {
      case _: Exception =>
        Stop
    }

    OneForOneStrategy()(defaultDecider)
  }
}

object TrackAnalysisManager {

  /**
    * Operation to dispatch analysis of some track
    *
    * @param track The track to be analyzed
    */
  case class Analyze(track: Track)

  /**
    * Response indicating the track sent for being analyzed is already being analyzed currently
    *
    * @param track The track already being analyzed
    */
  case class Pending(track: Track)

  /**
    * Message to give back the analyzed track
    *
    * @param track The insertion operation into the dao, yielding the track whose analysis have been done once
    *              the insertion is done
    */
  case class Done(track: Future[Track])
}
