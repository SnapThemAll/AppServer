package models.geometry

import junit.framework.Assert.assertEquals
import org.junit.Test

class PointTest {
  private val DELTA: Double = 0.000001

  @Test
  def testPointCreation() {
    val lat: Double = 23.04532
    val lon: Double = 02.34004
    val time: Long = 0L
    val p  = Point(lat, lon, time)
    assertEquals(p.latitude, lat, DELTA)
    assertEquals(p.longitude, lon, DELTA)
    assertEquals(p.time, time, DELTA)
  }

}
