package models.analysis

import models.geometry.Track

/**
  * Represent what the companion object of a case class extending TrackAnalysis needs.
  * A name for the parser/writer in Json and an apply function from TrackAnalyzer
  *
  * @tparam A The type of the analysis related to the companion object
  */
trait TrackAnalysisObject[A <: TrackAnalysis] extends WithJsonFormatter[A] {

  /**
    * Name used for the json formatter
    */
  val name: String = getClass.getSimpleName.replace("$", "")

  /**
    * Create a TrackAnalysis given an instance of TrackAnalyzer
    *
    * @param trackAnalyzer the TrackAnalyzer instance
    * @return the computed TrackAnalysis
    */
  def apply(trackAnalyzer: TrackAnalyzer): A
  def apply(track: Track): A = apply(TrackAnalyzer(track))
}
