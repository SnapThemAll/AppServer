package models

import java.time.ZonedDateTime
import java.util.UUID

import org.junit.Test

class TrackTest {

  def makeTrack(points: IndexedSeq[Point], date: ZonedDateTime): Track =
    Track(UUID.randomUUID(), UUID.randomUUID(), "", date, points, TrackAnalysisSet(), None)

  @Test
  def testConstructor() = {

    val l = IndexedSeq(Point(46.51702, 6.56563, 0L),
                       Point(46.52071, 6.58365, 500L),
                       Point(46.51786, 6.59790, 1000L),
                       Point(46.51786, 6.59790, 1500L),
                       Point(46.51432, 6.61131, 2000L),
                       Point(46.50675, 6.62680, 2500L),
                       Point(46.50657, 6.65449, 3000L))

    val tags = List("Hello", "Mountain")

    val tr =
      Track(java.util.UUID.fromString("a-b-c-d-e"),
            java.util.UUID.fromString("a-b-c-d-e"),
            "",
            ZonedDateTime.now(),
            l,
            TrackAnalysisSet(),
            Some(tags))

    assert(tr.trackID == java.util.UUID.fromString("a-b-c-d-e"))
    assert(tr.userID == java.util.UUID.fromString("a-b-c-d-e"))
    assert(tr.points == l)
    assert(tr.analyses.isEmpty)
    assert(tr.tags.contains(tags))
  }
}
