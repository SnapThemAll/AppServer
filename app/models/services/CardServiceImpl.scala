package models.services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
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

    userDAO.find(LoginInfo("facebook", fbID)).flatMap(user => {
      val name = user.map(_.name.getOrElse("Unnamed user")).getOrElse("Not found user")
      log(s"$name uploading a picture of $cardID in the database...")
      validationCategoryDAO.find(cardID).map(_.get)
        .flatMap{ validationCategory =>

          val (score, newValidationCat) = ScoreComputing.computeScore(descriptor, validationCategory)
          val picture = Picture(fileName, score)

          validationCategoryDAO.save(newValidationCat).flatMap{_ =>
            cardDAO.savePicture(fbID, cardID, picture)
              .map(_ => score)
          }
        }
    })
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
        cards.map(_.totalScore).sum
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
            .map{ user => computeTotalScore(user.loginInfo.providerKey).map(score => user.anonymousName -> score) }
        )
      }
  }

  override def removePicture(fbID: String, cardID: String, fileName: String): Future[Option[Card]] = {
    cardDAO.removePicture(fbID, cardID, fileName)
  }
}
