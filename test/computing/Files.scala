package computing


import java.io.{FileInputStream, FileOutputStream}

import scala.util.Try

object Files {

  type File = java.io.File
  def file(src: String): File = new File(src)

  def mv(src: File, dest: File): Boolean =
    Try(src.renameTo(dest)).getOrElse(false)

  def mv(src: File, dest: String): Boolean =
    Try(mv(src, file(dest))).getOrElse(false)

  def mv(src: String, dest: String): Boolean =
    Try(mv(file(src), file(dest))).getOrElse(false)

  def rename(src: File, newName: String): Boolean =
    mv(src, src.getParent + "/" + newName)

  def cp(src: File, dest: File): Boolean = {
    dest.isDirectory &&
      Try(new FileOutputStream(file(dest.getAbsolutePath + "/" + src.getName)).getChannel.transferFrom(
        new FileInputStream(src).getChannel, 0, Long.MaxValue) >= 0)
        .getOrElse(false)
  }

  def cp(src: File, dest: String): Boolean =
    Try(cp(src, file(dest))).getOrElse(false)

  def cp(src: String, dest: String): Boolean =
    Try(cp(file(src), file(dest))).getOrElse(false)

  def mkdir(src: File): Boolean =
    src.mkdir()
  def mkdir(src: String): Boolean =
    Try(mkdir(file(src))).getOrElse(false)

  def ls(dir: File): List[File] = {
    if (dir.exists && dir.isDirectory) {
      dir.listFiles.toList
    } else {
      Nil
    }
  }
  def ls(dir: String): List[File] = Try(ls(file(dir))).getOrElse(Nil)

  def findDuplicates(files: List[File], hashFunction: File => Long): List[List[File]] = {
    files.filter(_.isFile)
      .map { file => file -> hashFunction(file) }
      .groupBy(_._2)
      .values
      .map(_.map(_._1))
      .filter(_.size > 1)
      .toList
  }
}
