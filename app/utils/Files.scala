package utils

import java.io.{FileInputStream, FileOutputStream}

import scala.util.Try

object Files {

  type File = java.io.File
  def file(src: String): File = new File(src)

  def ls(dir: File): List[File] = {
    if (dir.exists && dir.isDirectory) {
      dir.listFiles.toList
    } else {
      Nil
    }
  }
  def ls(dir: String): List[File] = Try(ls(file(dir))).getOrElse(Nil)
}
