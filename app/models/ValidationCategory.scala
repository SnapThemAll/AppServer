package models

import play.api.libs.json.{Json, OFormat}
import utils.Logger
import utils.DataVariables.{getValidationDescriptor, pathToValidationImage}

case class ValidationCategory(
                               category: String,
                               validationPictures: Set[ValidationPicture],
                               averageGain: Float,
                               numberOfImprovements: Int
                             ) extends Logger {

  //val similaritiesScore: Float = validationPictures.map(_.highestSimilarity).sum


  def computeSimilarities(descriptors: Set[Descriptor]): ValidationCategory = {
    descriptors.foldLeft(this) { case (updatedValidationCategory, newPictureFP) =>
      updatedValidationCategory.computeSimilarites(newPictureFP)
    }
  }
  def computeSimilarites(descriptor: Descriptor): ValidationCategory = {
    val newValidationPictures = validationPictures.map{ valPic =>
      val similarity = getValidationDescriptor(category, valPic.fileName).similarityWith(descriptor)
      val newValPic = valPic.updateSimilarity(similarity)
      if(valPic.highestSimilarity > newValPic.highestSimilarity){
        error("OLD Validation picture cannot be greater than the NEW one")
      }
      newValPic
    }
    val newGain = newValidationPictures.map(_.highestSimilarity).sum - validationPictures.map(_.highestSimilarity).sum
    if(newGain < 0){
      error("old" + validationPictures.toString + "\n\t" +
        "new" + newValidationPictures.toString)
      error(s"#new = ${newValidationPictures.size}\n\t" +
        s"#old = ${validationPictures.size}")
      error(s"old similarities score: ${validationPictures.map(_.highestSimilarity).sum}\n\t" +
        s"new similarities score: ${newValidationPictures.map(_.highestSimilarity).sum}\n\t" +
        s"new - old = $newGain")
    }
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
    this.validationPictures.map(_.highestSimilarity).sum - that.validationPictures.map(_.highestSimilarity).sum
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