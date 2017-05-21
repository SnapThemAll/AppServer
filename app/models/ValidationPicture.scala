package models

import play.api.libs.json.{Json, OFormat}

case class ValidationPicture(
                              fileName: String,
                              highestSimilarity: Float
                            ) {

  def updateSimilarity(similarity: Float): ValidationPicture = {
    if(similarity > highestSimilarity){
      this.copy( highestSimilarity = similarity )
    } else {
      this
    }
  }

}

object ValidationPicture {

  implicit val jsonFormat: OFormat[ValidationPicture] = Json.format[ValidationPicture]
}