package jobs

import akka.actor.{Actor, Props}
import models.analysis._
import models.geometry.Track

/**
  * Performs the tracks analysis job and gives back the results. This is an actor, communicate with it using
  * messages.<br>
  * The supported operations are in the companion object
  */
private[jobs] class TrackAnalysisWorker extends Actor {
  import TrackAnalysisManager.Analyze
  import TrackAnalysisWorker._

  override def receive: Receive = {
    case Analyze(track) =>
      val analyzer = TrackAnalyzer(track)

      val analyses = TrackAnalysis.allTrackObjects.map{ analysis =>
        analysis.apply(analyzer)
      }.foldLeft(TrackAnalysisSet())(_ + _)

      /*
      val analyzed1 = track.addAnalysis(TrackEnergy(analyzer))
      sender ! AnalysisDone(analyzed1)
      val analyzed2 = analyzed1.addAnalysis(TrackTime(analyzer))
      sender ! AnalysisDone(analyzed2)
      val analyzed3 = analyzed2.addAnalysis(TrackDistance(analyzer))
      sender ! AnalysisDone(analyzed3)
      val analyzed4 = analyzed3.addAnalysis(TrackSpeed(analyzer))
      sender ! AnalysisDone(analyzed4)
      val analyzed5 = analyzed4.addAnalysis(TrackAltitude(analyzer))
      */

      sender ! WorkerDone(track.addAnalyses(analyses))
  }
}

private[jobs] object TrackAnalysisWorker {

  def props = Props[TrackAnalysisWorker]

  /**
    * Message to tell the manager that an analysis has been done. Others are eventually still running
    *
    * @param track The track whose analysis have been updated (containing the analysis)
    */
  case class AnalysisDone(track: Track)

  /**
    * Message to give back the analyzed track to the manager. This tells the manager that no other analysis for this
    * track is running <==> final message
    *
    * @param track The track whose analysis have been completed (containing the analysis)
    */
  case class WorkerDone(track: Track)
}
