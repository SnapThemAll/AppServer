package models.services

import com.google.inject.Inject
import models.Message
import models.daos.MessageDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class MessageServiceImpl @Inject()(messageDAO: MessageDAO) extends MessageService {

  override def get: Future[Message] = messageDAO.find.map(_.get)
}
