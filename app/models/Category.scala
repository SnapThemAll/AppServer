package models

/**
  * Created by Greg on 18.05.2017.
  */
trait Category {

  val name: String
  val picturesFP: Set[PictureFingerPrint]

}
