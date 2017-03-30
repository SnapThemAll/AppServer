package models.analysis

import play.api.libs.json.OFormat

trait WithJsonFormatter[A] {

  /**
    * An implicit model format to parse and write this class in Json
    */
  implicit val implicitModelFormat: OFormat[A]

}
