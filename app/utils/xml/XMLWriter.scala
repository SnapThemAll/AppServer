package utils.xml

import java.time.temporal.ChronoUnit

import models.analysis.TrackAnalysis
import models.geometry.Track

import scala.xml.Utility.trim
import scala.xml.{Node, NodeBuffer}

/**
  * Helper object to write classes as XML
  */
object XMLWriter {

  /**
    * Given a list of track and their respective prediction time write them as xml (following the gpx format)
    * If the prediction time is 0 or less, then the tag related to the prediction (as show below) will be omitted.
    * {{{<prd>timeInSecond<prd>}}}
    *
    * @param tracksWithPredictions the list of tracks and their respective prediction time in seconds to write
    * @return The xml of the tracks (following the gpx format)
    */
  def tracksWithPredictionsToXML(tracksWithPredictions: Seq[(Track, Double)]): Node = {
    val tracksAsXML = new NodeBuffer()

    tracksWithPredictions foreach {
      case (track, prediction) =>
        val pointsAsXML = new NodeBuffer()

        track.points.foreach { point =>
          pointsAsXML +=
            <trkpt lat={point.latitude.toString} lon={point.longitude.toString}>
            <time>
              {track.date.plus(point.time, ChronoUnit.MILLIS).toString}
            </time>
          </trkpt>
        }

        tracksAsXML +=
          <trk>
          <name>{track.name}</name>
          {if(prediction > 0)
            <prd>{prediction}</prd>
          }
          <trkseg>
            {pointsAsXML}
          </trkseg>
        </trk>
    }

    trim(
        <gpx>
          {tracksAsXML}
        </gpx>
    )
  }

  /**
    * Given a list of track write them a xml (following the gpx format)
    *
    * @param tracks the list of tracks to write
    * @return The xml of the tracks (following the gpx format)
    */
  def tracksToXML(tracks: Seq[Track]): Node = {
    tracksWithPredictionsToXML(tracks.map(t => (t, 0d)))
  }

  /**
    * Given a track write it as xml (following the gpx format)
    *
    * @param track the track to write
    * @return The xml of the track (following the gpx format)
    */
  def trackToXML(track: Track): Node = {
    tracksToXML(Seq(track))
  }

  /**
    * Given the analysis of a track, write it an xml extension (following the gpx format)
    *
    * @param trackAnalyses the analysis of a track to write
    * @return the xml of the analysis (following the gpx format)
    */
  def analysisToXML(trackAnalyses: Set[TrackAnalysis]): Node = {
    val analysesAsXML = new NodeBuffer()

    for (analysis <- trackAnalyses) {
      analysesAsXML += analysis.toXML
    }
    trim(
        <extensions>
          {analysesAsXML}
        </extensions>
    )
  }
}
