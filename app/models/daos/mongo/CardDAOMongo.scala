package models.daos.mongo


import com.google.inject.Inject
import models.Card
import models.daos.CardDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._

import scala.concurrent.Future
import scala.util.Random

/**
  * Give access to the [[Card]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
class CardDAOMongo @Inject()(mongoDB: Mongo) extends CardDAO {

  implicit val cardJsonFormat: OFormat[Card] = Card.jsonFormat

  private[this] def cardColl = mongoDB.collection("card")

  override def find(fbID: String, cardName: String): Future[Option[Card]] =
    cardColl.flatMap(
      _.find(Json.obj("fbID" -> fbID, "cardName" -> cardName)).one[Card]
    )

  override def findAll(fbID: String): Future[IndexedSeq[Card]] =
    cardColl.flatMap(
      _.find(Json.obj("fbID" -> fbID))
        .cursor[Card]()
        .collect[Seq](-1, Mongo.cursonErrorHandler[Card]("findAll in card dao"))
    ).map(_.toIndexedSeq)

  override def save(card: Card): Future[Card] =
    cardColl
      .flatMap(_.update(Json.obj("fbID" -> card.fbID, "cardName" -> card.cardName), card, upsert = true))
      .transform(
        _ => card,
        t => t
      )
  override def savePicture(fbID: String, cardName: String, fileName: String): Future[Double] = {
    find(fbID, cardName)
      .map{ cardAlreadyStored =>
        save(cardAlreadyStored.getOrElse(Card(cardName, fbID)).addPic(fileName, Random.nextDouble() * 10))
      }
      .flatMap{ cardSaved =>
        cardSaved.map(_.pictures.last.score)
      }
  }

  override def remove(fbID: String, cardName: String): Future[Unit] =
    cardColl
      .flatMap(_.remove(Json.obj("fbID" -> fbID, "cardName" -> cardName)))
      .transform(
        _ => (),
        t => t
      )


  override def removePicture(fbID: String, cardName: String, fileName: String): Future[Option[Card]] = {
    find(fbID, cardName)
      .map{cardAlreadyStored =>
        if(cardAlreadyStored.isEmpty || !cardAlreadyStored.get.getNotDeleted.pictures.exists(_.fileName == fileName)){
          Future.successful(None)
          }
        /*else if (cardAlreadyStored.get.pictures.length == 1 && cardAlreadyStored.get.pictures.head.fileName == fileName) {
           remove(fbID, cardName).map(_ => None)
        } */
        else {
          val cardToStore = cardAlreadyStored.get.removePic(fileName)
          save(cardToStore).map(Some(_))
        }
      }.flatMap(cardSaved => cardSaved)
  }

}
