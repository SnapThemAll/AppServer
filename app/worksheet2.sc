import java.util.UUID

import models.{ValidationCategory, ValidationPicture}

import scala.util.Random

def randomFileName: String = UUID.randomUUID().toString
def randomSimilarity: Float = Random.nextFloat() * 100000


val validationPicturesGenerated = List.fill(10)(ValidationPicture(randomFileName, randomSimilarity)).toSet
var validationCategoryGenerated = ValidationCategory(randomFileName, validationPicturesGenerated.toSet, 0f, 0)

def computeSimilarites(validationCategory: ValidationCategory): ValidationCategory = {
  val newValidationPictures = validationCategory.validationPictures.map{ valPic =>
    val similarity = randomSimilarity
    val newValPic = valPic.updateSimilarity(similarity)
    if(valPic.highestSimilarity > newValPic.highestSimilarity){
      println("OLD Validation picture cannot be greater than the NEW one")
    }
    newValPic
  }
  val newGain = newValidationPictures.map(_.highestSimilarity).sum - validationCategory.validationPictures.map(_.highestSimilarity).sum


  if(newGain < 0){
    println("old" + validationCategory.validationPictures.map(_.highestSimilarity).toString + "\n" +
      "new" + newValidationPictures.map(_.highestSimilarity).toString)
    println(s"#new = ${newValidationPictures.size}\n\t" +
      s"#old = ${validationCategory.validationPictures.size}")
    println(s"old similarities score: ${validationCategory.validationPictures.map(_.highestSimilarity).sum}\n\t" +
      s"new similarities score: ${newValidationPictures.map(_.highestSimilarity).sum}\n\t" +
      s"new - old = $newGain")
  }
  val newAverageGain = // WE SHOULD ONLY UPDATE AVERAGE GAIN IF THERE'S AN IMPROVEMENT
    if(newGain > 0) {
      (validationCategory.averageGain * validationCategory.numberOfImprovements + newGain) / (validationCategory.numberOfImprovements + 1)
    } else {
      validationCategory.averageGain
    }
  validationCategory.copy(
    validationPictures = newValidationPictures,
    averageGain = newAverageGain,
    numberOfImprovements = if(newGain > 0) validationCategory.numberOfImprovements + 1 else validationCategory.numberOfImprovements
  )
}

for{i <- 0 to 100000}{
  if(validationCategoryGenerated.validationPictures.map(_.highestSimilarity).sum > computeSimilarites(validationCategoryGenerated).validationPictures.map(_.highestSimilarity).sum)
    println("ERROR")
  validationCategoryGenerated = computeSimilarites(validationCategoryGenerated)
}
