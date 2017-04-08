package models.daos.mongo

import java.util.UUID

import com.google.inject.Inject
import models.Card
import models.daos.CardDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import reactivemongo.play.json._

import scala.concurrent.Future
import scala.util.Random

/**
  * Give access to the [[Card]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
class CardDAOMongo @Inject()(mongoDB: Mongo) extends CardDAO {

  implicit val cardJsonFormat = Card.jsonFormat

  private[this] def cardColl = mongoDB.collection("card")

  override def find(userID: UUID, cardName: String): Future[Option[Card]] =
    cardColl.flatMap(
      _.find(Json.obj("userID" -> userID, "cardName" -> cardName)).one[Card]
    )

  override def findAll(userID: UUID): Future[IndexedSeq[Card]] =
    cardColl.flatMap(
      _.find(Json.obj("userID" -> userID))
      .cursor[Card]()
      .collect[Seq](-1, Mongo.cursonErrorHandler[Card]("findAll in card dao"))
    ).map(_.toIndexedSeq)

  override def save(card: Card): Future[Card] =
    cardColl
      .flatMap(_.update(Json.obj("userID" -> card.userID, "cardName" -> card.cardName), card))
      .transform(
        _ => card,
        t => t
      )
  override def savePicture(userID: UUID, cardName: String, pictureURI: String): Future[Card] = {
    find(userID, cardName)
      .map{ cardAlreadyStored =>
        save(cardAlreadyStored.getOrElse(Card(cardName, userID, IndexedSeq(pictureURI), Random.nextDouble() * 10)))
      }.flatMap(cardSaved => cardSaved)
  }

  override def remove(userID: UUID, cardName: String): Future[Unit] =
    cardColl
      .flatMap(_.remove(Json.obj("userID" -> userID, "cardName" -> cardName)))
      .transform(
        _ => (),
        t => t
      )


  override def removePicture(userID: UUID, cardName: String, pictureURI: String): Future[Option[Card]] = {
    find(userID, cardName)
      .map{cardAlreadyStored =>
        if(cardAlreadyStored.isEmpty || (cardAlreadyStored.get.picturesURI.toSet - pictureURI).isEmpty){
          Future.successful(None)
        } else {
          val cardToStore = cardAlreadyStored.get
            .copy(picturesURI = (cardAlreadyStored.get.picturesURI.toSet - pictureURI).toIndexedSeq)
          save(cardToStore).map(Some(_))
        }
      }.flatMap(cardSaved => cardSaved)
  }

}
