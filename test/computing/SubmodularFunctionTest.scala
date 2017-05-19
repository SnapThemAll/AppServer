package computing

import models.PictureFingerPrint
import org.scalatestplus.play.PlaySpec

class SubmodularFunctionTest  extends PlaySpec {

  import ScoreComputing.computeScore
  import utils.Files._


  "Nearest Neighbor submodular function" should {
    "be computed correctly given a user set and validation set" in {
      val fp = PictureFingerPrint.fromImageFile(ls("data").filter(_.isFile).head)
      //println(computeScore(fp, "computer-monitor", Set()))
    }
  }

}
