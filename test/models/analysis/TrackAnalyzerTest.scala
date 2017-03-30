package models.analysis

import java.time.ZonedDateTime
import java.util.UUID

import breeze.plot._
import models.geometry.{GeoUtils, Point, Track}
import org.junit.{Assert, Test}

import scala.util.Random

class TrackAnalyzerTest {

  import Utils._

  @Test
  def speedAverageCosineNoNoise() = {
    val pts = for (i <- 0 until 500; x = 2 * Math.PI * i / 500d)
      yield Point(Math.cos(x), 0, 3600 * 1000 * i, 0)

    //plotData(pts.map(_.latitude), "/home/mario/plot_1.png")
    val radius = 1
    val track  = Track(UUID.randomUUID(), UUID.randomUUID(), "", ZonedDateTime.now, pts.toList.toIndexedSeq, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, euclideanDistanceNoAlt, euclideanDistanceWithAlt)

    //x(t) = cos(x)
    //v(t) = x'(t)
    //average = (integral v(t) from 0 to 2*pi)/2*pi = 0
    Assert.assertEquals(0d, ta.speedAverage, 0.01d)
  }

  @Test
  def burntCaloriesConstantSpeedWithGradient() = {

    //In the example from http://summitmd.com/pdf/pdf/090626_aps09_970.pdf p13
    //there is a slope of 10% and the speed is 160.8 meters/minutes
    //For every 10 meters, we climb 1 meter
    //This means we walked sqrt(101) meters
    //We need to do 160.8 m/min, so time = distance/speed
    //However, Point take time in milliseconds, so we have to multiply by 60'000
    //Moreover, distances are assumed to be in km, hence the /1000
    val pts = for (i <- 0 until 500)
      yield Point(10 * i / 1000d, 0, math.round(60000 * i * math.sqrt(101d) / 160.8), i / 1000d)
    val radius = 1
    val track  = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, euclideanDistanceNoAlt, euclideanDistanceWithAlt)

    //The following tests are meant to test the "preconditions" i.e. to ensure that the conversion are well done
    for (i <- ta.blurredSpeed.indices) {
      //because blurredSpeed returns km/h, not meters/minutes
      Assert.assertEquals(160.8d, ta.blurredSpeed(i) * 1000d / 60d, 2d)
      Assert.assertEquals(0.1d, ta.blurredSteepness(i), 0.0001d)
    }

    //Kcal/Min ~= respiratoryExchangeRatio * massKg * VO2 / 1000
    val respiratoryExchangeRatio = 4.86 //Constant when running
    val massKg                   = 1
    val v02                      = 50.13 //taken from the example quoted above
    val expected                 = respiratoryExchangeRatio * massKg * v02 * ta.trackTimeMinutes / 1000d

    Assert.assertEquals(expected, ta.estimatedEnergyBurnt(1d), 0.01d)
  }

  @Test
  def blurredAltitudeCosineNoNoise() = {
    val pts    = for (i <- 0 until 500; x = 2 * Math.PI * i / 500d) yield Point(x, 0, i, Math.cos(x))
    val radius = 20
    val track  = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, euclideanDistanceNoAlt, euclideanDistanceWithAlt)

    val blurredAltitude = ta.blurredAltitude

    for (i <- 0 until 500) {
      val x = 2 * Math.PI * i / 500d
      val y = Math.cos(x)
      Assert.assertEquals(y, blurredAltitude(i), 0.02d)
    }
  }

  @Test
  def blurredAltitudeCosineWithNoise() = {
    val pts = for (i <- 0 until 500; x = 2 * Math.PI * i / 500d)
      yield Point(x, 0, i, Math.cos(x) + Math.random() * 0.1)

    val radius = 20
    val track  = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, euclideanDistanceNoAlt, euclideanDistanceWithAlt)

    val blurredAltitude = ta.blurredAltitude

    for (i <- 50 until 450) {
      val x = 2 * Math.PI * i / 500d
      val y = Math.cos(x)
      Assert.assertEquals(y, blurredAltitude(i), 0.1d)
    }
  }

  @Test
  def blurredSteepnessCosineNoNoise() = {
    val pts    = for (i <- 0 until 500; x = 2 * Math.PI * i / 500d) yield Point(x, 0, i, Math.cos(x))
    val radius = 10
    val track  = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, euclideanDistanceNoAlt, euclideanDistanceWithAlt)

    val blurredSteepness = ta.blurredSteepness

    for (i <- 50 until 450) {
      val x = 2 * Math.PI * i / 500d
      //cos' = -sin
      val y = -Math.sin(x)
      Assert.assertEquals(y, blurredSteepness(i), 0.02d)
    }
  }

  @Test
  def blurredSteepnessCosineWithNoise() = {
    val pts = for (i <- 0 until 500; x = 2 * Math.PI * i / 500d)
      yield Point(x, 0, i, Math.cos(x) + Math.random() * 0.1)

    val radius = 30
    val track  = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, euclideanDistanceNoAlt, euclideanDistanceWithAlt)

    val blurredSteepness = ta.blurredSteepness

    for (i <- 100 until 400) {
      val x = 2 * Math.PI * i / 500d
      //cos' = -sin
      val y = -Math.sin(x)
      Assert.assertEquals(y, blurredSteepness(i), 0.1d)
    }
  }

  @Test
  def averageSlopeCaseOne() = {
    val pts    = for (i <- 0 until 500) yield Point(i, i, i, 1000 + i / 2d)
    val tags   = List("Hello", "Mountain")
    val radius = 3
    val track =
      Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), Some(tags))
    val ta = TrackAnalyzer(track, radius, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)

    Assert.assertEquals(.5d, ta.averageAltitudeDifference, 0.005)
  }

  @Test
  def averageSlopeCaseTwo() = {
    //Toblerone-line points
    val pts    = for (i <- 0 to 500) yield Point(i, i, i, 1000 + ((i % 2) * 500))
    val radius = 3
    val track  = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), None)
    val ta     = TrackAnalyzer(track, radius, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)

    Assert.assertEquals(0d, ta.averageAltitudeDifference, 0.005)
  }

  /**
  def mostTimeSpentCaseOne() = {
    val l = List(
      new Point(46.51702, 6.56563, 0.0),
      new Point(46.52071, 6.58365, 0.2),
      new Point(46.51786, 6.59790, 0.6),
      new Point(46.51786, 6.59790, 0.623),
      new Point(46.51432, 6.61131, 0.625),
      new Point(46.51432, 6.611312, 0.6251),
      new Point(46.51432, 6.611313, 0.6252),
      new Point(46.50675, 6.62680, 1.1d),
      new Point(46.50657, 6.65449, 1.2d))

    val tags = List("Hello", "Mountain")
    val blurRadius = 3
    val track = Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "",l.toIndexedSeq, None, Some(tags))
    val ta = new TrackAnalyzer(track, blurRadius, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)

    val res = ta.mostTimeSpent(3)
    Assert.assertEquals(naiveMostTimeSpent(track.avrgSpeeds(3).toVector, 3), res)
  }
    */
  @Test
  def mostTimeSpentCaseTwo() = {
    val pts = for (i <- 0 until 200) yield Point(randDouble(45, 46), randDouble(6, 7), i, 100)

    val tags   = List("Hello", "Mountain")
    val radius = 3
    val track =
      Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), Some(tags))
    val ta = TrackAnalyzer(track, radius, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)

    //println(track.avrgSpeeds(blurRadius))
    val nbPoints = 20
    Assert.assertEquals(naiveMostTimeSpent(ta.blurredSpeed, nbPoints), ta.mostTimeSpent(nbPoints))
  }

  @Test
  def mostTimeSpentCaseThree() = {
    val pts = for (i <- 0 until 10) yield Point(randDouble(45, 46), randDouble(6, 7), i, 100)

    val tags   = List("Hello", "Mountain")
    val radius = 5
    val track =
      Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), Some(tags))
    val ta = TrackAnalyzer(track, radius, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)

    //println(track.avrgSpeeds(blurRadius))
    val nbPoints = 9
    Assert.assertEquals(naiveMostTimeSpent(ta.blurredSpeed, nbPoints), ta.mostTimeSpent(nbPoints))
  }

  @Test
  def mostTimeSpentCaseFour() = {
    val pts = for (i <- 0 until 1000) yield Point(randDouble(45, 46), randDouble(6, 7), i, 100)

    val tags   = List("Hello", "Mountain")
    val radius = 13
    val track =
      Track(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), "", ZonedDateTime.now, pts, TrackAnalysisSet(), Some(tags))
    val ta = TrackAnalyzer(track, radius, GeoUtils.distanceHaversine, GeoUtils.distanceWithAltDiff)

    //println(track.avrgSpeeds(blurRadius))
    val nbPoints = 800
    Assert.assertEquals(naiveMostTimeSpent(ta.blurredSpeed, nbPoints), ta.mostTimeSpent(nbPoints))
  }

  private object Utils {
    private val rdm = new Random()

    def randDouble(min: Double, max: Double) = {
      val diff = max - min
      rdm.nextDouble() * diff + min
    }

    def naiveMostTimeSpent(speed: IndexedSeq[Double], nbPoints: Int) = {
      var worst = (0, Double.PositiveInfinity)
      for (i <- 0 until speed.size - nbPoints + 1) {
        val actual = speed.slice(i, i + nbPoints).sum
        if (actual < worst._2) {
          worst = (i, actual)
        }
      }

      (worst._1, worst._1 + nbPoints)
    }

    def euclideanDistanceWithAlt(p1: Point, p2: Point) = {
      val diffX = p1.longitude - p2.longitude
      val diffY = p1.latitude - p2.latitude
      val diffZ = p1.elevation - p2.elevation
      Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ)
    }

    def euclideanDistanceNoAlt(p1: Point, p2: Point) = {
      val diffX = p1.longitude - p2.longitude
      val diffY = p1.latitude - p2.latitude
      Math.sqrt(diffX * diffX + diffY * diffY)
    }

    def plotData(data: IndexedSeq[Double], filename: String) = {
      val f = Figure()
      val p = f.subplot(0)
      val x = breeze.linalg.linspace(0, data.length, data.length)
      p += plot(x, data)
      f.saveas(filename)
    }
  }

}
