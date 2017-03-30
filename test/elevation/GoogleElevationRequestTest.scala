package elevation

import models.elevation.GoogleElevationRequest
import org.junit.Assert._
import org.junit.Test

class GoogleElevationRequestTest {
  private val BIG_DELTA: Double = 5

  @Test
  def getAltitudeReturnsCorrectValue() {

    val altitudeManager = new GoogleElevationRequest()
    altitudeManager.addLocation(45.56359940950819, 7.959380149841309)
    val altitudes: Seq[Double] = altitudeManager.build()
    val onlineComputedValue: Double = 733.714
    assertEquals(altitudes(0), onlineComputedValue, BIG_DELTA)
  }

  //TODO: Remove this comment just before last version, we don't want to lose requests now

  /*@Test
  def getAltitudeReturnsCorrectValueFor200Location() {

    val altitudeManager = new GoogleElevationRequest()
    for(i <- (0 until 208)) {

      if(i == 108) {
        altitudeManager.addLocation(45.86960, 6.32813)
      }
      else if(i == 203){
        altitudeManager.addLocation(58.50900, 104.06250)
      }
      else {
        altitudeManager.addLocation(45.56359940950819, 7.959380149841309)
      }
    }
    val altitudes: Seq[Double] = altitudeManager.build()
    val onlineComputedValue: Double = 733.714
    assertEquals(altitudes(0), onlineComputedValue, BIG_DELTA)
    assertEquals(771.6 , (altitudes.toArray).apply(108), BIG_DELTA)
    assertEquals(569.0 , (altitudes.toArray).apply(203), BIG_DELTA)
    assertTrue(altitudes.size == 208)
  }*/
}
