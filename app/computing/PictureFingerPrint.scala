package computing

import org.bytedeco.javacpp.opencv_core.Mat
import OpenCVUtils._
import models.Picture

case class PictureFingerPrint(rows: Int, cols: Int, `type`: Int, data: IndexedSeq[Int]) {
  require(data.length == rows * cols, "Matrix length should equal rows * cols")
  require(`type` == 0, "Matrix element type should be CV_8U")

  private lazy val descriptor : Mat = buildDescriptor(rows, cols, `type`, data)

  def distanceWith(that: PictureFingerPrint): Float = distance(this.descriptor, that.descriptor)
}

object PictureFingerPrint {

  def fromDescriptor(descriptor: Mat): PictureFingerPrint =
    PictureFingerPrint(descriptor.rows, descriptor.cols, descriptor.`type`, matToIndexedSeq(descriptor))

  def fromImage(imagePath: String): PictureFingerPrint = fromDescriptor(computeDescriptor(imagePath))

  def fromPicture(picture: Picture): PictureFingerPrint = fromImage(picture.fileName)
}
