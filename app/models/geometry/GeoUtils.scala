package models.geometry


object GeoUtils {

  /*Nasa factsheet earth

    Equatorial radius (km)	        6378.137
    Polar radius (km)               6356.752
    Volumetric mean radius (km)     6371.008
    Core radius (km)                3485
    Ellipticity (Flattening)        0.00335

    TODO: get accurate earth radius at position of runner to calculate more exact distance with above data=> vincenty iterative formula(For after christmas)
   */

  private[this] final val R: Double                  = 6371.008

  /**
    * @param p1 first point
    * @param p2 second point
    * @return the distance in km between two geographic points "as the crow flies"
    */
  def distanceHaversine(p1: Point, p2: Point): Double = {

    val dLat: Double    = degreeToRad(p2.latitude - p1.latitude)
    val dLon: Double    = degreeToRad(p2.longitude - p1.longitude)
    val sinDLon: Double = math.sin(dLon / 2d)
    val a: Double =
      math.sin(dLat / 2d) * math.sin(dLat / 2d) +
        math.cos(degreeToRad(p1.latitude)) * math.cos(degreeToRad(p2.latitude)) * sinDLon * sinDLon

    val c: Double = 2d * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    (R * c)
  }

  /**
    * @param p1 first point
    * @param p2 second point
    * @return the real distance between the two points taking altitude difference in account
    */
  def distanceWithAltDiff(p1: Point, p2: Point) = {
    val d = distanceHaversine(p1, p2)
    //must be in km
    val altDiff = math.abs(p2.elevation - p1.elevation) / 1000d

    math.sqrt(d * d + altDiff * altDiff)
  }

  def distanceOfSubTrack(subTrack: List[Point]) = {

    def subTrackLoop(subTrack: List[Point], acc: Double): Double = subTrack match {
      case Nil          => acc
      case _ :: Nil     => acc
      case x :: y :: xs => subTrackLoop(y :: xs, acc + GeoUtils.distanceWithAltDiff(x, y))
    }
    subTrackLoop(subTrack, 0)
  }

  private def degreeToRad(degree: Double): Double = degree * (math.Pi / 180d)
}
