package models.analysis

import breeze.integrate._
import concurrent.Parallel._
import models.geometry._


/**
  * A class that analyses a track
  *
  * @param track The track to be analysed
  * @param blurRadius The number of points for blurring the data.
  *               A higher number implies smoother data. This should be less than the number of recorded points.
  * @param distanceNoAltitude A function that computes the distance between two points,
  *                           without taking into account the altitude (in km)
  * @param distanceWithAltitude A function that computes the distance between two points,
  *                             with taking into account the altitude (in km)
  */
case class TrackAnalyzer(track: Track,
                         blurRadius: Int = 20,
                         distanceNoAltitude: (Point, Point) => Double = GeoUtils.distanceHaversine,
                         distanceWithAltitude: (Point, Point) => Double = GeoUtils.distanceWithAltDiff) {
  import Utils._
  val trackPoints   = track.points.toIndexedSeq
  val nbPointsTrack = trackPoints.length

  /**
    * Represents the speeds at the recorded points (in km/h)
    */
  lazy val blurredSpeed = blurData(
    blurRadius,
    trackPoints,
    averageCombining(
      (p1: Point, p2: Point) => distanceWithAltitude(p1, p2) / TimeUtils.millisToHours(p2.time - p1.time)
    )
  )

  /**
    * Represents the distance at the recorded points (in km)
    */
  lazy val blurredTotalDistance = blurData(
    blurRadius,
    trackPoints,
    averageCombining(
      (p1: Point, p2: Point) => distanceWithAltitude(p1, p2)
    )
  ).sum

  /**
    * Represents the altitude at the recorded points (in km)
    */
  lazy val blurredAltitude = blurData(
      blurRadius,
      trackPoints,
      noCombining(
          (middle: Point) => middle.elevation
      )
  )

  /**
    * Represents the altitude difference between successive pairs of points (in km)
    */
  lazy val blurredAltitudeDifference = blurData(
      blurRadius,
      blurredAltitude, //not trackPoints !!!
      averageCombining(
          (p1: Double, p2: Double) => p2 - p1
      )
  )

  /**
    * Represents the distance difference between successive pairs of points,
    * without taking into account the altitude difference (in km)
    */
  lazy val blurredDistancesNoAltitude = blurData(
      blurRadius,
      trackPoints,
      averageCombining(
          (p1: Point, p2: Point) => distanceNoAltitude(p1, p2)
      )
  )

  /**
    * Represents the steepness at each points
    */
  lazy val blurredSteepness = {
    blurredDistancesNoAltitude.zip(blurredAltitudeDifference).map {
      case (distance, altitude) =>
        altitude / distance
    }
  }

  /**
    * The minimum speed of the track (in km/h)
    */
  lazy val minSpeed = blurredSpeed.min

  /**
    * The maximum speed of the track (in km/h)
    */
  lazy val maxSpeed = blurredSpeed.max

  /**
    * The duration of this track (in millis)
    */
  lazy val trackTimeMillis = track.points.last.time

  /**
    * The duration of this track (in hours)
    */
  lazy val trackTimeHours = TimeUtils.millisToHours(trackTimeMillis)

  /**
    * The duration of this track (in minutes)
    */
  lazy val trackTimeMinutes = TimeUtils.millisToMinutes(trackTimeMillis)

  /**
    * Computes the average denivelation of the track
    */
  lazy val averageAltitudeDifference = blurredAltitudeDifference.sum / nbPointsTrack

  /**
    * Compute the speed average of the track (in km/h)
    */
  lazy val speedAverage = {
    val tasks = for (range <- splittedArrayIndexes)
      yield
        task {
          var acc = 0d
          var i   = range._1
          while (i < range._2) {
            acc += blurredSpeed(i)
            i += 1
          }
          acc
        }
    tasks.map(_.join()).sum / blurredSpeed.size
  }

  /**
    * Compute the speed variance of the track
    * @return the speed variance of the track
    */
  lazy val speedVariance = {
    val avg = speedAverage
    val tasks = for (range <- splittedArrayIndexes)
      yield
        task {
          var acc = 0d
          var i   = range._1
          while (i < range._2) {
            val diff = blurredSpeed(i) - avg
            acc += diff * diff
            i += 1
          }
          acc
        }

    tasks.map(_.join()).sum / blurredSpeed.size
  }

  /**
    * Computes the interval where the sum of the speeds is minimal.
    * This can be interpreted as the interval where the runner has spent the most of their time running.
    *
    * @param nbPoints The number of points in the interval
    * @return A pair representing the interval where the sum of the speeds are minimal
    */
  def mostTimeSpent(nbPoints: Int) = {
    require(0 < nbPoints && nbPoints <= blurredSpeed.size)
    val nbSentinels   = nbPoints - 1
    val sentinelSpeed = blurredSpeed ++ Vector.fill(nbSentinels)(Double.PositiveInfinity)

    val tasks = for (range <- splittedArrayIndexes)
      yield
        task {
          val offsetBegin = range._1
          val offsetEnd   = range._2

          var worstSoFar = {
            var i   = 0
            var acc = 0d

            while (i < nbPoints) {
              acc = acc + sentinelSpeed(offsetBegin + i)
              i = i + 1
            }
            (offsetBegin, acc)
          }

          var idx       = offsetBegin + 1
          var lastValue = worstSoFar._2
          while (idx < offsetEnd) {
            val currentValue = lastValue + sentinelSpeed(idx + nbPoints - 1) - sentinelSpeed(idx - 1)
            if (currentValue < worstSoFar._2) {
              worstSoFar = (idx, currentValue)
            }
            lastValue = currentValue
            idx = idx + 1
          }
          worstSoFar
        }
    val result = tasks.map(_.join).minBy(_._2)
    (result._1, result._1 + nbPoints)
  }

  /**
    * Computes the estimated energy burnt by the runner.
    *
    * @param mass The mass of the runner of this track (in kg)
    * @return The estimated energy burnt, in kcal
    * @see http://certification.acsm.org/metabolic-calcs for more information
    */
  def estimatedEnergyBurnt(mass: Double) = {
    val respiratoryExchangeRatio = 4.86
    val ratio                    = trackTimeMinutes / nbPointsTrack

    //km/h -> meters/mins : 1000d/60d
    val speedFunction = (x: Double) =>
      if (x * ratio >= nbPointsTrack) blurredSpeed.last * 1000d / 60d
      else blurredSpeed(math.ceil(x * ratio).toInt) * 1000d / 60d

    val gradeFunction = (x: Double) =>
      if (x * ratio >= nbPointsTrack) blurredSteepness.last
      else blurredSteepness(math.ceil(x * ratio).toInt)

    val vo2Function = (x: Double) => 0.2 * speedFunction(x) + 0.9 * speedFunction(x) * gradeFunction(x) + 3.5

    val totalV02 = trapezoid(vo2Function, 0, trackTimeMinutes, nbPointsTrack * 4)

    //See http://fitness.stackexchange.com/questions/15608/energy-expenditure-calories-burned-equation-for-running
    respiratoryExchangeRatio * mass * totalV02 / 1000d
  }

  private val splittedArrayIndexes = {
    val numTask = 8 // TODO : should change
    val strip   = blurredSpeed.size / numTask
    val rest    = blurredSpeed.size - strip * numTask

    val nbPointsPerTask = Seq
      .fill(numTask)(strip)
      .zipWithIndex
      .map {
        case (nbPoints, index) =>
          //redistribute the rest
          if (index < rest) nbPoints + 1
          else nbPoints
      }
      .filter(_ != 0)
    assert(nbPointsPerTask.sum == blurredSpeed.size)

    //create indexes s.t. begin is inclusive and end is exclusive
    val result = nbPointsPerTask
      .foldLeft(List[(Int, Int)]()) {
        case (previous, nbPoints) =>
          previous match {
            case Nil =>
              List((0, nbPoints))
            case (_, previousEnd) :: _ =>
              (previousEnd, previousEnd + nbPoints) :: previous
          }
      }
      .reverse

    assert(result.head._1 == 0 && result.last._2 == blurredSpeed.size)

    result
  }

  private object Utils {

    //Copy/Paste from Track.scala, with minor changes

    def noCombining[T](dataFunction: T => Double): (Option[T], T, Option[T]) => Double = {
      case (_, middle, _) => dataFunction(middle)
    }

    def averageCombining[T](combineTwoPoints: (T, T) => Double): (Option[T], T, Option[T]) => Double = {
      case (Some(left), middle, Some(right)) =>
        (combineTwoPoints(left, middle) + combineTwoPoints(middle, right)) / 2d
      case (None, middle, Some(right)) =>
        combineTwoPoints(middle, right)
      case (Some(left), middle, None) =>
        combineTwoPoints(left, middle)
      case (None, middle, None) =>
        combineTwoPoints(middle, middle) //not sure about this
    }

    def blurData[T](radius: Int,
                    points: IndexedSeq[T],
                    combine: (Option[T], T, Option[T]) => Double): IndexedSeq[Double] = {
      require(radius > 0, "Radius must be greater than zero.")

      val blurred = new Array[Double](points.length)

      for (i <- points.indices) {
        val left = {
          if (i - 1 >= 0)
            Some(points(i - 1))
          else None
        }
        val right = {
          if (i + 1 < points.length)
            Some(points(i + 1))
          else None
        }
        val middle = points(i)

        blurred(i) = combine(left, middle, right)
      }

      val numTasks = 32

      if (radius > 1) {
        val newBlurred = new Array[Double](blurred.length)
        parallelBlur(blurred, newBlurred, numTasks, radius)
        newBlurred.toIndexedSeq
      } else {
        blurred.toIndexedSeq
      }
    }

    def blur(src: Array[Double], dst: Array[Double], from: Int, end: Int, radius: Int): Unit = {
      for (i <- from until end) {
        dst(i) = kernel(src, i, radius)
      }
    }

    /**
      * Blurs the arrays using a given blurRadius
      */
    def parallelBlur(src: Array[Double], dst: Array[Double], numTasks: Int, radius: Int): Unit = {
      val sWidth = math.max(src.length / numTasks, 1)
      val l      = src.indices.by(sWidth) :+ src.length
      val strips = l zip l.tail
      val tasks = for ((start, end) <- strips) yield {
        task {
          blur(src, dst, start, end, radius)
        }
      }
      tasks.foreach(_.join)
    }

    def kernel(src: Array[Double], index: Int, radius: Int): Double = {
      var i, j    = -radius
      var average = 0d
      var count   = 0
      while (i <= radius) {
        if (index + i >= 0 && index + i < src.length) {
          average += src(index + i)
          count = count + 1
        }
        i = i + 1
      }
      average / count
    }
  }
}
