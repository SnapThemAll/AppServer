package utils

import models.Descriptor
import utils.Files.ls

import scala.collection.immutable.Queue


object DataVariables extends Logger {

  val absolutePathToData: String = "/home/snap/data/"

  private val validationDir = absolutePathToData + "validation/"
  private val sampleDir = absolutePathToData + "sample/"
  private val clutterDir = absolutePathToData + "clutter/"

  val categories: Set[String] = ls(validationDir).map(_.getName).toSet

  private var cache: Queue[(String, Set[Descriptor])] = Queue.empty
  private var cacheSize = 60
  def cacheIsFull: Boolean = !(cache.size < cacheSize)
  def getValidationDescriptors(category: String): Set[Descriptor] = {
    cache.find( _._1 == category ).map(_._2).getOrElse{
      if(cache.size <= cacheSize){
        val descriptors = listValidationFileNames(category)
          .map(fileName => Descriptor.fromImagePath(pathToValidationImage(category, fileName)))
        cache = cache.enqueue(category -> descriptors)
        descriptors
      } else {
        cache = cache.dequeue._2
        getValidationDescriptors(category)
      }
    }
  }

  def getValidationDescriptor(category: String, fileName: String): Descriptor = {
    getValidationDescriptors(category).find(_.fileName == fileName).get
  }

  def computeClutterDescriptors: Set[Descriptor] = {
    ls(clutterDir).map(Descriptor.fromImageFile).toSet
  }

  def listValidationFileNames(category: String): Set[String] =
    listFileNames(pathToValidationFolder(category))

  def listSampleFileNames(category: String): Set[String] =
    listFileNames(pathToSampleFolder(category))


  def listFileNames(dir: String): Set[String] = ls(dir).filter(_.isFile).map(_.getName).toSet

  def pathToFolder(fbID: String, cardID: String): String =
    absolutePathToData + s"users/$fbID/$cardID/"

  def pathToImage(fbID: String, cardID: String, fileName: String): String =
    absolutePathToData + s"users/$fbID/$cardID/$fileName"

  private def pathToValidationFolder(category: String): String =
    s"$validationDir$category/"

  private def pathToValidationImage(category: String, fileName: String): String =
    pathToValidationFolder(category) + fileName

  def pathToSampleFolder(category: String): String =
    s"$sampleDir$category/"

  def pathToSampleImage(category: String, fileName: String): String =
    pathToSampleFolder(category) + fileName

}
