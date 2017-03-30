package models.elevation

import scala.collection.mutable
import scala.io.Source._

import play.api.libs.json._

class GoogleElevationRequest(private[this] val requests: mutable.Queue[String] = mutable.Queue(),
                             private[this] val requestsBuilder: StringBuilder = new StringBuilder()) {

  private[this] final val ELEVATION_BASE_URL: String = "https://maps.googleapis.com/maps/api/elevation/json?locations="
  private[this] final val API_KEY: String            = "&key=AIzaSyBqhWjNpILFBRXbPOfoh3cwdetsQ6hyaA4"
  private[this] final val REQUEST_SIZE: Int          = 50
  private var locationsCounter                       = 0
  private var requestCounter                         = 0

  def addLocation(latitude: Double, longitude: Double): Unit = {

    if (locationsCounter < REQUEST_SIZE && locationsCounter > 0) {
      requestsBuilder.append('|')
    } else if (locationsCounter >= REQUEST_SIZE) {
      requestsBuilder.append(API_KEY)
      requests.enqueue(ELEVATION_BASE_URL + requestsBuilder.toString())
      locationsCounter = 0
      requestsBuilder.clear()
    }

    requestsBuilder.append(latitude)
    requestsBuilder.append(',')
    requestsBuilder.append(longitude)
    locationsCounter = locationsCounter + 1
  }

  def build(): Seq[Double] = {

    if (requestsBuilder.nonEmpty) {
      requestsBuilder.append(API_KEY)
      requests.enqueue(ELEVATION_BASE_URL + requestsBuilder.toString())
      requestsBuilder.clear()
    }

    def queueLoop(acc: Seq[Double]): Seq[Double] = {
      //requests are cleared
      if (requests.nonEmpty) requests.dequeue() match {
        case x =>
          print(x)
          queueLoop(acc ++ urlRequest(x))
      } else {
        acc
      }
    }

    queueLoop(Seq())
  }

  private def urlRequest(url: String): Seq[Double] = {

    if (requestCounter >= 50) {
      //We are limited to 50 request per second
      Thread.sleep(1000)
      requestCounter = 0
    }

    val response: JsValue = Json.parse(fromURL(url).mkString)
    requestCounter = requestCounter + 1

    val altitudes: Seq[Double] = (response \ "status").as[String] match {

      case "OK" =>
        implicit val altReader = Json.reads[Elevation]
        (response \ "results").as[Seq[Elevation]].map(x => x.elevation)

      case "INVALID_REQUEST" =>
        throw new IllegalArgumentException("INVALID_REQUEST in altitude request Elevation API returned")

      case "OVER_QUERY_LIMIT" =>
        throw new IllegalArgumentException("OVER_QUERY_LIMIT in altitude request Elevation API returned, wait a day")

      case "REQUEST_DENIED" =>
        throw new IllegalArgumentException("INVALID_REQUEST in altitude request Elevation API returned")

      case "UNKNOWN_ERROR" =>
        throw new IllegalArgumentException("UNKNOWN_ERROR in altitude request Elevation API return")
    }

    require(altitudes.size <= REQUEST_SIZE, "altitudes.size = " + altitudes.size)

    altitudes
  }

  private case class Elevation(elevation: Double, location: JsObject, resolution: Double)

}
