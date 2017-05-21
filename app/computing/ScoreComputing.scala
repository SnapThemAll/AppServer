package computing

import models.{Descriptor, ValidationCategory}
import utils.Logger

object ScoreComputing extends Logger {

  def computeScore(newDescriptor: Descriptor, validationCategory: ValidationCategory) : (Float, ValidationCategory) = {

    val updatedValidationCategory = validationCategory.computeSimilarites(newDescriptor)
    val marginalGain = updatedValidationCategory.marginalGain(validationCategory)

    val score = marginalGain / updatedValidationCategory.averageGain // SCORE = GAIN / AVERAGE GAIN

    log(s"Category ${validationCategory.category} improved by $marginalGain} (the bigger the better). Score: $score")

    score -> updatedValidationCategory
  }

}
