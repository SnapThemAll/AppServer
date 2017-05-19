package models

import play.api.libs.json.{Json, OFormat}

case class ValidationPicture(
                              pictureFP: PictureFingerPrint,
                              highestSimilarity: Float
                            ) {

  def computeSimilarity(newPictureFP: PictureFingerPrint): ValidationPicture = {
    this.updateSimilarity(this.pictureFP.similarityWith(newPictureFP))
  }

  private def updateSimilarity(similarity: Float): ValidationPicture = {
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