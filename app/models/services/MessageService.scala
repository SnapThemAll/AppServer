package models.services

import models.Message

import scala.concurrent.Future

trait MessageService {

  def get: Future[Message]

}
