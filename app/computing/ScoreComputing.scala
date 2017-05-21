package computing

import models.{PictureFingerPrint, ValidationCategory}
import utils.Logger

object ScoreComputing extends Logger {

  def computeScore(newPictureFP: PictureFingerPrint, validationCategory: ValidationCategory) : (Float, ValidationCategory) = {

    val updatedValidationCategory = validationCategory.computeSimilarites(newPictureFP)
    val marginalGain = updatedValidationCategory.marginalGain(validationCategory)

    val score = marginalGain / updatedValidationCategory.averageGain // SCORE = GAIN / AVERAGE GAIN

    log(s"Category ${validationCategory.name} improved by $marginalGain} (the bigger the better). Score: $score")

    score -> updatedValidationCategory
  }

}
