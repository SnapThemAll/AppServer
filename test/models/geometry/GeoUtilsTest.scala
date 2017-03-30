package models.geometry

import junit.framework.Assert._
import org.junit.Assert.assertEquals
import org.junit.Test


class GeoUtilsTest {

  //Online calculators can have a different value
  private val BIG_DELTA: Double = 5
  private val SMALL_DELTA: Double = 0.005

  @Test
  def distanceSamePoint() {
    val lat1: Double = 0d
    val lon1: Double = 0d
    val lat2: Double = 0d
    val lon2: Double = 0d
    val computedValue: Double = 0d
    val d: Double = GeoUtils.distanceHaversine(Point(lat1, lon1, 0L, 0d), Point(lat2, lon2, 0L, 0d))
    assertEquals(d, computedValue, SMALL_DELTA)
  }

  @Test
  def smallDistanceRolexWidth() {
    val lat1: Double = 46.51780
    val lon1: Double = 6.56727
    val lat2: Double = 46.51782
    val lon2: Double = 6.56942
    val onlineComputedValue: Double = 0.168
    val d: Double = GeoUtils.distanceHaversine(Point(lat1, lon1, 0L, 0d), Point(lat2, lon2, 0L, 0d))
    assertEquals(d, onlineComputedValue, SMALL_DELTA)
  }

  @Test
  def distanceLatLonCorrectVal1() {
    val lat1: Double = 0d
    val lon1: Double = 0d
    val lat2: Double = 13.4656
    val lon2: Double = 20.2938
    val onlineComputedValue: Double = 2689
    val d: Double = GeoUtils.distanceHaversine(Point(lat1, lon1, 0L, 0d), Point(lat2, lon2, 0L, 0d))
    assertEquals(d, onlineComputedValue, BIG_DELTA)
  }

  @Test
  def distanceLatLonCorrectVal2() {
    val lat1: Double = 10.2
    val lon1: Double = 0.34
    val lat2: Double = 02.34
    val lon2: Double = 12.43
    val onlineComputedValue: Double = 1595
    val d: Double = GeoUtils.distanceHaversine(Point(lat1, lon1, 0L, 0d), Point(lat2, lon2, 0L, 0d))
    assertEquals(d, onlineComputedValue, BIG_DELTA)
  }

  @Test
  def realDistanceWithAltitude() {
    val lat1: Double = 10.2
    val lon1: Double = 0.34
    val lat2: Double = 02.34
    val lon2: Double = 12.43
    val d: Double = GeoUtils.distanceWithAltDiff(Point(lat1, lon1, 0L), Point(lat2, lon2, 0L))
    assertTrue(d >= GeoUtils.distanceHaversine(Point(lat1, lon1, 0L), Point(lat2, lon2, 0L)))
  }

}
