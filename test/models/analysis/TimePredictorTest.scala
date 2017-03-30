package models.analysis

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.UUID

import models.geometry.{Point, Track}
import org.junit.Test

class TimePredictorTest {

  val distanceWeight  = 1
  val dateWeight      = 0.01
  val steepnessWeight = 0.01
  val bandwidth       = 40
  val numParam        = 3
  val resolution      = 100

  val p = Parameters(distanceWeight, dateWeight, steepnessWeight, bandwidth, numParam)

  def makeTrack(points: IndexedSeq[Point], date: ZonedDateTime): Track =
    Track(UUID.randomUUID(), UUID.randomUUID(), "", date, points, TrackAnalysisSet(), None)

  @Test
  def tracksDecomposition(): Unit = {

    val newTrack = makeTrack(IndexedSeq(Point(46.702, 6.56563, 0L, 0),
                                        Point(46.52071, 6.58365, 500L, 0),
                                        Point(46.53786, 6.59790, 1000L, 0.01),
                                        Point(46.51786, 6.59790, 1500L, 0.02),
                                        Point(46.55432, 6.61131, 2000L, 0.03)),
                             ZonedDateTime.ofInstant(Instant.ofEpochMilli(832000000L), ZoneId.systemDefault()))

    val tr = new TimePredictor(newTrack, p, resolution)

    val t1 = IndexedSeq(Point(46.51702, 6.56563, 0L, 0),
                        Point(46.52071, 6.58365, 500L, 0.01),
                        Point(46.51786, 6.59790, 1000L, 0.01),
                        Point(46.51786, 6.59790, 1500L, 0.02),
                        Point(46.51432, 6.61131, 2000L, 0.03),
                        Point(46.50675, 6.62680, 2500L, 0.04),
                        Point(46.50657, 6.65449, 3000L, 0.05))

    val t2 = IndexedSeq(Point(46.517, 6.5656, 0L, 0),
                        Point(46.5207, 6.5836, 500L, 0.02),
                        Point(46.5178, 6.5979, 1000L, 0.01),
                        Point(46.5178, 6.5979, 1500L, 0.02),
                        Point(46.5143, 6.6113, 2000L, 0.03),
                        Point(46.506, 6.6268, 2500L, 0.04),
                        Point(46.5065, 6.6544, 300L, 0.05))

    val tr1 = makeTrack(t1, ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault()))
    val tr2 = makeTrack(t2, ZonedDateTime.ofInstant(Instant.ofEpochMilli(432000000L), ZoneId.systemDefault()))
  }
}
