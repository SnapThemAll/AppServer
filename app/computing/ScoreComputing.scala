package computing

import models.{Descriptor, ValidationCategory}
import utils.Logger

import scala.util.Random

object ScoreComputing extends Logger {

  def computeScore(newDescriptor: Descriptor, validationCategory: ValidationCategory) : (Float, ValidationCategory) = {

    val updatedValidationCategory = validationCategory.computeSimilarites(newDescriptor)
    val marginalGain = updatedValidationCategory.marginalGain(validationCategory)

    var score: Float = marginalGain / updatedValidationCategory.averageGain // SCORE = GAIN / AVERAGE GAIN

    log(s"Category ${validationCategory.category} improved by $marginalGain} (the bigger the better). Score: $score")


    score = Math.min(score * 5, 10f) // [0, 10]

    if(score > 0 && score < 1){
      score = 1f + randomScore(1f, .5f) // a little help if score is between [0, 1[
    }
    if(score == 0){
      score += randomScore(.2f, 1f) // a little help (sometimes) if score == 0
    }

    score -> updatedValidationCategory
  }

  def randomScore(percentage: Float, to: Float): Float = {
    if(Random.nextFloat() < percentage) {
      Random.nextFloat() * to
    } else {
      0f
    }
  }

}
