package models

import play.api.libs.json.{Json, OFormat}

case class ValidationCategory(
                               name: String,
                               validationPictures: Set[ValidationPicture],
                               averageGain: Float,
                               numberOfImprovements: Long
                             ) extends Category {

  override val picturesFP: Set[PictureFingerPrint] = validationPictures.map(_.pictureFP)

  val similaritiesScore: Float = validationPictures.map(_.highestSimilarity).sum

  def computeSimilarites(newPictureFP: PictureFingerPrint): ValidationCategory = {
    val newValidationPictures = validationPictures.map(_.computeSimilarity(newPictureFP))
    val newGain = newValidationPictures.map(_.highestSimilarity).sum - this.similaritiesScore
    val newAverageGain = // WE SHOULD ONLY UPDATE AVERAGE GAIN IF THERE'S AN IMPROVEMENT
      if(newGain > 0) {
        (averageGain * numberOfImprovements + newGain) / (numberOfImprovements + 1)
      } else {
        averageGain
      }
    this.copy(
      validationPictures = newValidationPictures,
      averageGain = newAverageGain,
      numberOfImprovements = if(newGain > 0) numberOfImprovements + 1 else numberOfImprovements
    )
  }

  def marginalGain(that: ValidationCategory): Float = {
    this.similaritiesScore - that.similaritiesScore
  }
}

object ValidationCategory {

  def initFromCategory(category: Category): ValidationCategory = {
    ValidationCategory(
      category.name,
      category.picturesFP.map(pFP => ValidationPicture(pFP, 0f)),
      0f,
      0l
    )
  }

  implicit val jsonFormat: OFormat[ValidationCategory] = Json.format[ValidationCategory]

}