package models.daos

import models.Message

import scala.concurrent.Future

/**
  * Created by Greg on 19.05.2017.
  */
trait MessageDAO {

  def save(message: Message): Future[Message]

  def find: Future[Option[Message]]

}
