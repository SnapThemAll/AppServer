package computing

import java.io.File

import org.bytedeco.javacpp.indexer.UByteRawIndexer
//import org.bytedeco.javacpp.opencv_core.{NORM_HAMMING, DMatch, DMatchVector, Mat, KeyPointVector}
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_features2d.{BFMatcher, ORB}
import org.bytedeco.javacpp.opencv_imgcodecs.{IMREAD_COLOR, imread}

object OpenCVUtils {

  def computeDescriptor(file: File): Mat = {
    computeDescriptor(loadOrExit(file))
  }

  def distance(descriptor1: Mat, descriptor2: Mat, numberToSelect: Int = 15): Float = {
    // Create feature matcher
    val matcher = new BFMatcher(NORM_HAMMING, false)
    val matches = new DMatchVector()
    matcher.`match`(descriptor1, descriptor2, matches)

    //compute distance over the best n matches
    distanceOfBestMatches(matches, numberToSelect)
  }

  def buildDescriptor(rows: Int, cols: Int, `type`: Int, data: IndexedSeq[Int]): Mat = {
    require(data.length == rows * cols, "Matrix length should equal rows * cols")
    require(`type` == 0, "Matrix element type should be CV_8U")

    `type` match {
      case 0 => // CV_8U type => UByteRawIndexer
        val mat = new Mat(rows, cols, `type`)
        val indexer = mat.createIndexer().asInstanceOf[UByteRawIndexer]

        for((value, index) <- data.zipWithIndex){
          indexer.put(0, 0, index, value)
        }

        mat
    }
  }

  def matToIndexedSeq(mat: Mat): IndexedSeq[Int] = {
    require(mat.`type`() == 0, "Matrix element type should be CV_8U")

    val indexer = mat.createIndexer().asInstanceOf[UByteRawIndexer]
    val size = mat.total.toInt
    for (i <- 0 until size) yield indexer.get(0, 0, i)
  }

  private def loadOrExit(file: File, flags: Int = IMREAD_COLOR): Mat = {
    // Read input image
    val image = imread(file.getAbsolutePath, flags)
    if (image.empty()) {
      println("Couldn't load image: " + file.getAbsolutePath)
      sys.exit(1)
    }
    image
  }

  private def computeDescriptor(imageMat: Mat): Mat = {
    val keyPoint = new KeyPointVector()
    val descriptor = new Mat()

    val orb = ORB.create()

    // Detect ORB features and compute descriptors for both images
    orb.detect(imageMat, keyPoint)
    orb.compute(imageMat, keyPoint, descriptor)

    descriptor
  }

  private def distanceOfBestMatches(matches: DMatchVector, numberToSelect: Int): Float = {
    // Convert to Scala collection, and sort
    val sorted = toArray(matches)//.sortWith(_ lessThan _)

    sorted.map(_.distance()).sortWith(_ < _).take(numberToSelect).sum
  }

  private def toArray(matches: DMatchVector): Array[DMatch] = {
    // for the simplicity of the implementation we will assume that number of key points is within Int range.
    require(matches.size() <= Int.MaxValue)
    val n = matches.size().toInt

    // Convert keyPoints to Scala sequence
    for (i <- Array.range(0, n)) yield new DMatch(matches.get(i))
  }

}
