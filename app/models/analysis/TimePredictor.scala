package models.analysis

import breeze.linalg.{DenseMatrix, DenseVector, pinv}
import models.geometry.{GeoUtils, Point, Track}

case class TimePredictor(newTrack: Track, p: Parameters, resolution: Double = 100) {

  require(resolution > 0.0, "Resolution should be superior to zero")

  def computePrediction(prevTracks: Seq[Track]): Double = {

    val dataInputs = prevTracks.flatMap(scrapTrack).toArray

    val dataOutputs = decomposeTrack(newTrack)

    //Time estimation of all the independent parts of the track we want a prediction on
    val results = for (sub <- dataOutputs) yield lwrPredictTime(dataInputs, sub)

    val finalTime = results.sum
    finalTime
  }

  //DecomposeParts can't ignore any parts
  private[this] def decomposeTrack(track: Track): List[SubTrack] = {
    val ta          = TrackAnalyzer(track, 1, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)
    val smoothTrack = ta.blurredSteepness

    val pointsWithAltDiff = track.points.toList zip smoothTrack

    def decomposeLoop(points: List[(Point, Double)], acc: List[SubTrack]): List[SubTrack] = points match {
      case Nil      => acc
      case _ :: Nil => acc
      case x :: y :: xs =>
        val segment   = points.takeWhile(point => math.abs(x._2 - point._2) < resolution)
        val lengthSeg = segment.length

        if (lengthSeg < 2) {
          //Slope of point is forced to slope of next point to avoid Subtracks with only 1 point and distance zero
          decomposeLoop((x._1, y._2) :: y :: xs, acc)
        } else {
          val dist      = GeoUtils.distanceOfSubTrack(segment.map(_._1))
          val time      = math.abs(segment.last._1.time - segment.head._1.time)
          val slopeAvrg = segment.map(_._2).sum / lengthSeg.toDouble
          val subTrack  = SubTrack(dist, time, track.date.toInstant.toEpochMilli, slopeAvrg)
          decomposeLoop(points drop lengthSeg, subTrack :: acc)
        }
    }
    decomposeLoop(pointsWithAltDiff, Nil)
  }

  //ScrapParts can ignore some parts if they are too small
  private[this] def scrapTrack(track: Track): List[SubTrack] = {
    val ta          = TrackAnalyzer(track, 1, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)
    val smoothTrack = ta.blurredSteepness

    val pointsWithAltDiff = track.points.toList zip smoothTrack

    def decomposeLoop(points: List[(Point, Double)], acc: List[SubTrack]): List[SubTrack] = points match {
      case Nil => acc
      case x :: _ =>
        val segment   = points.takeWhile(y => math.abs(x._2 - y._2) < resolution)
        val lengthSeg = segment.length

        val dist = GeoUtils.distanceOfSubTrack(segment.map(_._1))

        //Subtracks must be at Least 500 meters long
        if (dist < 0.5) {
          decomposeLoop(points drop lengthSeg, acc)
        } else {
          val time      = math.abs(segment.last._1.time - segment.head._1.time)
          val slopeAvrg = segment.map(_._2).sum / lengthSeg.toDouble
          val subTrack  = SubTrack(dist, time, track.date.toInstant.toEpochMilli, slopeAvrg)
          decomposeLoop(points drop lengthSeg, subTrack :: acc)
        }
    }
    val parts = decomposeLoop(pointsWithAltDiff, Nil)

    //Better to have the whole track if no parts could be scraped from track
    if (parts == Nil) {
      val time      = math.abs(track.points.last.time - track.points.head.time)
      val slopeAvrg = smoothTrack.sum / smoothTrack.length.toDouble
      List(SubTrack(TrackDistance(track).totDistance, time, track.date.toInstant.toEpochMilli, slopeAvrg))
    } else {
      parts
    }
  }

  private[this] def lwrPredictTime(prevTracks: Array[SubTrack], newTrack: SubTrack): Double = {

    val m       = prevTracks.length
    val weights = weightMatrix(prevTracks, newTrack)

    val inputsMatrix: DenseMatrix[Double] = DenseMatrix.tabulate(m, p.n) {
      case (i, j) => math.pow(prevTracks(i).distance, j)
    }

    val outputsVector: DenseVector[Double] = DenseVector.tabulate(m) {
      case (i) => prevTracks(i).time
    }

    val firstPart                  = inputsMatrix.t * (weights * inputsMatrix)
    val betas: DenseVector[Double] = pinv(firstPart) * (inputsMatrix.t * (weights * outputsVector))

    val predictD: DenseVector[Double] = DenseVector.tabulate(p.n) {
      case (i) => math.pow(newTrack.distance, i)
    }

    predictD.t * betas
  }

  private[this] def gaussianKernelWeight(oldTrack: SubTrack, newTrack: SubTrack): Double = {

    //Use of "Kernel trick" here
    val phi = DenseVector(math.abs(oldTrack.distance - newTrack.distance),
                          dateDiffInDays(oldTrack.date),
                          math.abs(oldTrack.slopeLevel - newTrack.slopeLevel))

    val weightMatrix: DenseMatrix[Double] =
      DenseMatrix((p.distanceWeight, 0d, 0d), (0d, p.dateWeight, 0d), (0d, 0d, p.slopeWeight))

    val Q: Double = phi dot (weightMatrix * phi)

    math.exp(-Q / (2 * p.bandwidth * p.bandwidth))
  }

  private[this] def weightMatrix(tracks: Array[SubTrack], newTrack: SubTrack): DenseMatrix[Double] = {

    val m = tracks.length

    val weights: DenseMatrix[Double] = DenseMatrix.zeros(m, m)

    for (i <- 0 until m) {
      weights.update(i, i, gaussianKernelWeight(tracks(i), newTrack))
    }

    weights
  }

  private[this] def dateDiffInDays(date: Long): Double =
    math.abs(date - newTrack.date.toInstant.toEpochMilli) / (60 * 60 * 24 * 1000)
}

case class Parameters(distanceWeight: Double = 1,
                      dateWeight: Double = 0.01,
                      slopeWeight: Double = 0.01,
                      bandwidth: Double = 40,
                      n: Int = 3)

private case class SubTrack(distance: Double, time: Double, date: Long, slopeLevel: Double)
