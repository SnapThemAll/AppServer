package computing

import org.scalatestplus.play.PlaySpec

import scala.util.Try

class SubmodularFunctionTest  extends PlaySpec {

  import ComputingUtils._
  import ScoreComputing._
  import Files._
  import OpenCVUtils._

  "Nearest Neighbor submodular function" should {
    "be computed correctly given a user set and validation set" in {
      println(fNN(userClutterSet, validationSet))
      println(fNN(userSet, validationSet))
      println(fNN(userSet, validationSet))
    }
  }

}
