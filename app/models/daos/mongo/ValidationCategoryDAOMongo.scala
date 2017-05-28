package models.daos.mongo

import com.google.inject.{Inject, Singleton}
import models.ValidationCategory
import models.daos.ValidationCategoryDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, OFormat}
import reactivemongo.play.json._

import scala.concurrent.Future


/**
  * Give access to the [[ValidationCategory]] object.
  * Uses ReactiveMongo to access the MongoDB database
  */
@Singleton
class ValidationCategoryDAOMongo @Inject()(mongoDB: Mongo) extends ValidationCategoryDAO {

  implicit val validationCategoryJsonFormat: OFormat[ValidationCategory] = ValidationCategory.jsonFormat

  private[this] def validationCategoryColl = mongoDB.collection("validation_category")

  override def save(validationCategory: ValidationCategory): Future[ValidationCategory] = {
      validationCategoryColl
        .flatMap(_.update(Json.obj("category" -> validationCategory.category), validationCategory, upsert = true))
        .transform(
          _ => validationCategory,
          t => t
        )
  }

  override def find(category: String): Future[Option[ValidationCategory]] = {
      validationCategoryColl.flatMap(
      _.find(Json.obj("category" -> category)).one[ValidationCategory]
    )
  }
}

