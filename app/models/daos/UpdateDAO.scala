package models.daos

import models.Update

import scala.concurrent.Future

/**
  * Created by Greg on 19.05.2017.
  */
trait UpdateDAO {

  def save(update: Update): Future[Update]

  def find: Future[Option[Update]]

}
