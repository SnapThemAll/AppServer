package models

import play.api.libs.json.{Json, OFormat}


case class Picture(
                    fileName: String,
                    score: Double,
                    deleted: Boolean = false
                  ) {
  def delete: Picture = this.copy(deleted = true)
}

object Picture{

  /**
    * An implicit writer to export a picture to JSON. Import this in your scope to use Json.toJson on a picture object.
    */
  implicit val jsonFormat: OFormat[Picture] = Json.format[Picture]
}