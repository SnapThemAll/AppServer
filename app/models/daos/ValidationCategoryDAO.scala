package models.daos

import models.ValidationCategory

import scala.concurrent.Future

/**
  * Created by Greg on 19.05.2017.
  */
trait ValidationCategoryDAO {

  def save(validationCategory: ValidationCategory): Future[ValidationCategory]

  def find(categoryName: String): Future[Option[ValidationCategory]]

}
