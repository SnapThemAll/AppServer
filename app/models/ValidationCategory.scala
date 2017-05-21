package models

import play.api.libs.json.{Json, OFormat}
import utils.Logger
import utils.DataVariables.pathToValidationImage

case class ValidationCategory(
                               category: String,
                               validationPictures: Set[ValidationPicture],
                               averageGain: Float,
                               numberOfImprovements: Int
                             ) extends Logger {

  val similaritiesScore: Float = validationPictures.map(_.highestSimilarity).sum


  def computeSimilarities(descriptors: Set[Descriptor]): ValidationCategory = {
    descriptors.foldLeft(this) { case (updatedValidationCategory, newPictureFP) =>
      updatedValidationCategory.computeSimilarites(newPictureFP)
    }
  }
  def computeSimilarites(descriptor: Descriptor): ValidationCategory = {
    val newValidationPictures = validationPictures.map{valPic =>
      val similarity = Descriptor.fromImagePath(pathToValidationImage(category, valPic.fileName)).similarityWith(descriptor)
      valPic.updateSimilarity(similarity)}
    val newGain = newValidationPictures.map(_.highestSimilarity).sum - this.similaritiesScore
    log(s"old similarities score: $similaritiesScore\n\t" +
      s"new similarities score: ${newValidationPictures.map(_.highestSimilarity).sum}\n\t" +
      s"new - old = $newGain")
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

  def initFromCategory(category: String, fileNames: Set[String]): ValidationCategory = {
    ValidationCategory(
      category,
      fileNames.map(fileName => ValidationPicture(fileName, 0f)),
      0f,
      0
    )
  }

  implicit val jsonFormat: OFormat[ValidationCategory] = Json.format[ValidationCategory]

}