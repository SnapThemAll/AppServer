package models.analysis

import scala.xml.Node

import play.api.libs.json.{JsValue, Json, OFormat}

/**
  * Represent an analysis of track
  */
sealed trait TrackAnalysis {

  /**
    * The XML representation of the track analysis is used to send data to the app.
    *
    * @return xml represention of the track analysis.
    */
  def toXML: Node

  /**
    * A simple name of the analysis class
    */
  val name: String = getClass.getSimpleName
}

object TrackAnalysis extends WithJsonFormatter[TrackAnalysis]  {

  /**
    * Unapply is used by play to write a case class to the Json format.
    * Note : We need to edit this if a track analysis type is added.
    */
  def unapply(trackAnalysis: TrackAnalysis): Option[(String, JsValue)] = {
    val tuple = trackAnalysis match {
      case e: TrackSpeed    => (e.name, Json.toJson(e)(TrackSpeed.implicitModelFormat))
      case e: TrackTime     => (e.name, Json.toJson(e)(TrackTime.implicitModelFormat))
      case e: TrackEnergy   => (e.name, Json.toJson(e)(TrackEnergy.implicitModelFormat))
      case e: TrackAltitude => (e.name, Json.toJson(e)(TrackAltitude.implicitModelFormat))
      case e: TrackDistance => (e.name, Json.toJson(e)(TrackDistance.implicitModelFormat))
      // if you add a case here, remember to add the companion object in the val "allTrackObjects"
    }
    Some(tuple)
  }

  /**
    * Apply is used by play to parse a case class from the Json format.
    * Note : We need to edit this if a track analysis type is added.
    */
  def apply(`class`: String, data: JsValue): TrackAnalysis = {
    val analysisTypeObject =
      allTrackObjects
        .find(_.name == `class`)
        .getOrElse(
            throw new IllegalArgumentException(
                "Cannot retrieve this TrackAnalysis class. It should be added in the apply function of TrackAnalysis"
            )
        )

    Json.fromJson(data)(analysisTypeObject.implicitModelFormat).get
  }

  //this list MUST contain all analyses' companion object
  val allTrackObjects = Seq(TrackSpeed, TrackTime, TrackEnergy, TrackAltitude, TrackDistance)

  override implicit val implicitModelFormat: OFormat[TrackAnalysis] = Json.format[TrackAnalysis]
}

/**
  * Represent the speeds information of a track
  *
  * @param minSpeed The minimum speed on the track.
  * @param maxSpeed The maximum speed on the track.
  * @param avrgSpeed The average speed on the track.
  * @param varianceSpeed The variance on the speed on the track.
  * @param speeds The speed for each points of a track
  */
case class TrackSpeed(minSpeed: Double,
                      maxSpeed: Double,
                      avrgSpeed: Double,
                      varianceSpeed: Double,
                      speeds: IndexedSeq[Double])
    extends TrackAnalysis {
  override def toXML: Node =
    <speed-analysis>
      <min>{minSpeed}</min>
      <max>{maxSpeed}</max>
      <avrg>{avrgSpeed}</avrg>
      <var>{varianceSpeed}</var>
      <speed-at-points>{speeds.mkString(" ")}</speed-at-points>
    </speed-analysis>

}

object TrackSpeed extends TrackAnalysisObject[TrackSpeed] {

  override implicit val implicitModelFormat: OFormat[TrackSpeed] = Json.format[TrackSpeed]

  override def apply(trackAnalyzer: TrackAnalyzer) = {
    TrackSpeed(
        trackAnalyzer.minSpeed,
        trackAnalyzer.maxSpeed,
        trackAnalyzer.speedAverage,
        trackAnalyzer.speedVariance,
        trackAnalyzer.blurredSpeed
    )
  }
}

/**
  * Represent the time information of a track
  *
  * @param trackDuration time spent running a track in seconds
  * @param mostTimeSpentLeft left point index of where we lose the most of our time
  * @param mostTimeSpentRight right point index of where we lose the most of our time
  */
case class TrackTime(trackDuration: Long, mostTimeSpentLeft: Int, mostTimeSpentRight: Int) extends TrackAnalysis {
  override def toXML: Node =
    <time-analysis>
      <duration>{trackDuration}</duration>
      <mostTimeSpentL>{mostTimeSpentLeft}</mostTimeSpentL>
      <mostTimeSpentR>{mostTimeSpentRight}</mostTimeSpentR>
    </time-analysis>
}
object TrackTime extends TrackAnalysisObject[TrackTime] {

  override implicit val implicitModelFormat: OFormat[TrackTime] = Json.format[TrackTime]

  override def apply(trackAnalyzer: TrackAnalyzer): TrackTime = {
    val (mostTimeSpentLeft, mostTimeSpentRight) =
      trackAnalyzer.mostTimeSpent(trackAnalyzer.blurredSpeed.length / 10 + 2)
    TrackTime(
        trackAnalyzer.trackTimeMillis,
        mostTimeSpentLeft,
        mostTimeSpentRight
    )
  }
}

/**
  * Represent the altitude information of a track
  *
  * @param altitudes the list of altitudes for each point of track track in km
  * @param positiveAltDiff the total positive elevation point by point of the track in km
  * @param negativeAltDif the total negative elevation point by point of the track in km
  */
case class TrackAltitude(altitudes: Seq[Double], positiveAltDiff: Double, negativeAltDif: Double)
    extends TrackAnalysis {
  override def toXML =
    <altitude-analysis>
      <altitudes-per-point>{altitudes.mkString(" ")}</altitudes-per-point>
      <positiveAltDiff>{positiveAltDiff}</positiveAltDiff>
      <negativeAltDiff>{negativeAltDif}</negativeAltDiff>
    </altitude-analysis>
}

object TrackAltitude extends TrackAnalysisObject[TrackAltitude] {

  override implicit val implicitModelFormat: OFormat[TrackAltitude] = Json.format[TrackAltitude]

  override def apply(trackAnalyzer: TrackAnalyzer): TrackAltitude = {
    val (positiveAltDif, negativeAltDif) = trackAnalyzer.blurredAltitudeDifference.partition(_ > 0)

    TrackAltitude(
        trackAnalyzer.track.points.map(_.elevation),
        positiveAltDif.sum,
        -negativeAltDif.sum
    )
  }
}

/**
  * Represent the energy information of a track
  *
  * @param caloriesBurnt: estimated calories burnt in kcal
  */
case class TrackEnergy(caloriesBurnt: Double) extends TrackAnalysis {
  override def toXML: Node =
    <energy-analysis>
      <cal>{caloriesBurnt}</cal>
    </energy-analysis>
}

object TrackEnergy extends TrackAnalysisObject[TrackEnergy] {

  override implicit val implicitModelFormat: OFormat[TrackEnergy] = Json.format[TrackEnergy]

  //TODO define a way to get the mass of the user
  private val DEFAULT_MASS: Double = 70
  override def apply(trackAnalyzer: TrackAnalyzer): TrackEnergy =
    TrackEnergy(trackAnalyzer.estimatedEnergyBurnt(DEFAULT_MASS))
}

/**
  * Represent the distance information of a track
  *
  * @param totDistance total distance of the track
  */
case class TrackDistance(totDistance: Double) extends TrackAnalysis {

  override def toXML =
    <distance-analysis>
      <totalTrackDistance>{totDistance}</totalTrackDistance>
    </distance-analysis>
}

object TrackDistance extends TrackAnalysisObject[TrackDistance] {

  override implicit val implicitModelFormat: OFormat[TrackDistance] = Json.format[TrackDistance]

  override def apply(trackAnalyzer: TrackAnalyzer): TrackDistance = TrackDistance(trackAnalyzer.blurredTotalDistance)
}
