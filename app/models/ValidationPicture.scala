package models

import play.api.libs.json.{Json, OFormat}

case class ValidationPicture(
                              pictureFP: PictureFingerPrint,
                              highestSimilarity: Float
                            ) {

  def updateSimilarity(newPictureFP: PictureFingerPrint): ValidationPicture = {
    val similarity = this.pictureFP.similarityWith(newPictureFP)
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