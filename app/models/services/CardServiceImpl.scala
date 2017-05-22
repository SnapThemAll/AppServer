package models.services

import javax.inject.Inject

import computing.ScoreComputing
import models._
import models.daos.{CardDAO, UserDAO, ValidationCategoryDAO}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.Logger

import scala.concurrent.Future

/**
  * Handles actions to cards.
  *
  * @param cardDAO The card DAO implementation.
  * @param validationCategoryDAO The validationCategory DAO implementation
  */
class CardServiceImpl @Inject()(
                                 cardDAO: CardDAO,
                                 userDAO: UserDAO,
                                 validationCategoryDAO: ValidationCategoryDAO
                               ) extends CardService with Logger {

  override def savePicture(fbID: String, cardID: String, fileName: String, descriptor: Descriptor): Future[Double] = {
    log(s"Starts fetching category $cardID from the database...")
    validationCategoryDAO.find(cardID).map(_.get)
      .flatMap{ validationCategory =>

        log(s"Category $cardID fetched. Starts Computing the score...")
        val (score, newValidationCat) = ScoreComputing.computeScore(descriptor, validationCategory)
        val picture = Picture(fileName, score)

        log(s"Score computed. Saving $cardID category in the database...")
        validationCategoryDAO.save(newValidationCat).flatMap{_ =>
          log(s"Category $cardID saved. Sending the score back.")
          cardDAO.savePicture(fbID, cardID, picture)
            .map(_ => score)
        }
      }
  }

  override def retrieve(fbID: String, cardID: String): Future[Option[Card]] = {
    cardDAO.find(fbID, cardID).map{ maybeCard =>
      maybeCard.map(_.getNotDeleted).filter(_.pictures.nonEmpty)
    }
  }

  override def retrievePicture(fbID: String, cardID: String, fileName: String): Future[Option[Picture]] = {
    retrieve(fbID, cardID).map{ maybeCard =>
      maybeCard.flatMap(_.pictures.find(_.fileName == fileName))
    }
  }

  override def retrieveAll(fbID: String): Future[Seq[Card]] = {
    cardDAO.findAllCards(fbID).map{ cards =>
      cards.map(_.getNotDeleted).filter(_.pictures.nonEmpty)
    }
  }

  override def computeTotalScore(fbID: String): Future[Double] = {
    cardDAO.findAllCards(fbID)
      .map{ cards =>
        cards.map(_.bestScore).sum
      }
  }

  override def computeTotalScoreAllUsers(): Future[Seq[(String, Double)]] = {
//    cardDAO.findAll().map{ allCards =>
//      allCards
//        .groupBy(_.fbID)
//        .map{ case(fbID, cards) => fbID -> cards.map(_.bestScore).sum }
//    }
    userDAO.findAll()
      .flatMap{ users =>
        Future.sequence(
          users
            .map{ user => computeTotalScore(user.loginInfo.providerID).map(score => user.name.getOrElse("No Name") -> score) }
        )
      }
  }

  override def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]] = {
    cardDAO.removePicture(fbID, cardID, fileName)
  }
}
