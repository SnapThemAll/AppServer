package models

import java.io.File

import computing.OpenCVUtils.{computeDescriptor, similarity}
import org.bytedeco.javacpp.opencv_core.Mat

/**
  * Created by Greg on 21.05.2017.
  */
case class Descriptor(fileName: String, descriptorMatrix: Mat) {

  def similarityWith(that: Descriptor): Float =
    similarity(this.descriptorMatrix, that.descriptorMatrix)
}

object Descriptor {

  def fromImageFile(imageFile: File): Descriptor = Descriptor(imageFile.getName, computeDescriptor(imageFile))

  def fromImagePath(imagePath: String): Descriptor = fromImageFile(new File(imagePath))

}