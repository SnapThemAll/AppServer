package models.services

import scala.concurrent.Future

trait UpdateService {

  def userNeedToUpdateApp(version: String): Future[Boolean]
  def serverIsUpdating(): Future[Boolean]

}
