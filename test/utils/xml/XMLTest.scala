package utils.xml

import java.time.ZonedDateTime
import java.util.UUID

import models.analysis.{TrackAnalysisSet, TrackSpeed, TrackTime}
import models.geometry.{Point, Track}

import scala.xml.Utility.trim

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class XMLTest extends PlaySpec {

  lazy val date  = ZonedDateTime.parse("2016-11-28T17:00:00.001Z")
  lazy val start = date.toInstant.toEpochMilli
  lazy val points = IndexedSeq(
      Point(47.1, -123.2, date.toInstant.toEpochMilli - start, 70.83922576904297),
      Point(48, -122, ZonedDateTime.parse("2016-11-28T17:00:02.050Z").toInstant.toEpochMilli - start, 162.8259429931641),
      Point(47.64, -122.543, ZonedDateTime.parse("2016-11-28T17:00:03.964Z").toInstant.toEpochMilli - start, 51.95919036865234)
  )
  lazy val trackAsXML =
    trim(
        <gpx>
          <trk>
            <name>TrackName</name>
            <trkseg>
              <trkpt lat="47.1" lon="-123.2">
                <time>2016-11-28T17:00:00.001Z</time>
              </trkpt>
              <trkpt lat="48.0" lon="-122.0">
                <time>2016-11-28T17:00:02.050Z</time>
              </trkpt>
              <trkpt lat="47.64" lon="-122.543">
                <time>2016-11-28T17:00:03.964Z</time>
              </trkpt>
            </trkseg>
          </trk>
          <trk>
            <name>TrackName2</name>
            <trkseg>
              <trkpt lat="47.1" lon="-123.2">
                <time>2016-12-28T17:00:00.001Z</time>
              </trkpt>
              <trkpt lat="48.0" lon="-122.0">
                <time>2016-12-28T17:00:02.050Z</time>
              </trkpt>
              <trkpt lat="47.64" lon="-122.543">
                <time>2016-12-28T17:00:03.964Z</time>
              </trkpt>
            </trkseg>
          </trk>
      </gpx>
    )

  lazy val speedAnalysisAsXML = trim(
      <speed-analysis>
        <min>{speedAnalysis.minSpeed}</min>
        <max>{speedAnalysis.maxSpeed}</max>
        <avrg>{speedAnalysis.avrgSpeed}</avrg>
        <var>{speedAnalysis.varianceSpeed}</var>
        <speed-at-points>{speedAnalysis.speeds.mkString(" ")}</speed-at-points>
      </speed-analysis>
  )
  lazy val timeAnalysisAsXML = trim(
      <time-analysis>
        <duration>{timeAnalysis.trackDuration}</duration>
        <mostTimeSpentL>{timeAnalysis.mostTimeSpentLeft}</mostTimeSpentL>
        <mostTimeSpentR>{timeAnalysis.mostTimeSpentRight}</mostTimeSpentR>
      </time-analysis>
  )

  lazy val speedAnalysis = TrackSpeed(1, 5, 3, 2, IndexedSeq(1.0, 3.0, 5.0))
  lazy val timeAnalysis  = TrackTime(3600, 0, 1)

  lazy val (nameParsed, dateParsed, pointsParsed) = XMLParser.nameDatePointsFromString(trackAsXML.toString).head
  lazy val track1 =
    Track(UUID.randomUUID(), UUID.randomUUID(), nameParsed, dateParsed, pointsParsed, TrackAnalysisSet(), None)

  lazy val (nameParsed2, dateParsed2, pointsParsed2) = XMLParser.nameDatePointsFromString(trackAsXML.toString)(1)
  lazy val track2 =
    Track(UUID.randomUUID(), UUID.randomUUID(), nameParsed2, dateParsed2, pointsParsed2, TrackAnalysisSet(), None)

  "One or more tracks " should {
    " be parsed correctly " in {
      nameParsed shouldBe "TrackName"
      dateParsed shouldBe date
      pointsParsed shouldBe points
    }

    "be parsed and then written to xml again with no changes" in {
      val trackWritten = XMLWriter.tracksToXML(Seq(track1, track2))

      trackWritten shouldBe trackAsXML
    }
    "have his analysis parsed correctly" in {
      lazy val result = XMLWriter.analysisToXML(Set(speedAnalysis, timeAnalysis))

      result.toString should include(speedAnalysisAsXML.toString)
      result.toString should include(timeAnalysisAsXML.toString)
    }
  }
}
