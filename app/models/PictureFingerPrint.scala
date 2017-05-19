package models

import java.io.File

import computing.OpenCVUtils.{buildDescriptor, computeDescriptor, similarity, matToIndexedSeq}
import org.bytedeco.javacpp.opencv_core.Mat
import play.api.libs.json.{Json, OFormat}

case class PictureFingerPrint(rows: Int, cols: Int, `type`: Int, data: IndexedSeq[Int]) {
  require(data.length == rows * cols, "Matrix length should equal rows * cols")
  require(`type` == 0, "Matrix element type should be CV_8U")
  require(cols == 32, "Feature elements should have dimension of 32")

  private lazy val descriptor : Mat = buildDescriptor(rows, cols, `type`, data)

  def similarityWith(that: PictureFingerPrint): Float = similarity(this.descriptor, that.descriptor)
}

object PictureFingerPrint {

  def fromDescriptor(descriptor: Mat): PictureFingerPrint =
    PictureFingerPrint(descriptor.rows, descriptor.cols, descriptor.`type`, matToIndexedSeq(descriptor))

  def fromImageFile(imageFile: File): PictureFingerPrint = fromDescriptor(computeDescriptor(imageFile))

  def fromImagePath(imagePath: String): PictureFingerPrint = fromImageFile(new File(imagePath))


  implicit val jsonFormat: OFormat[PictureFingerPrint] = Json.format[PictureFingerPrint]
}
