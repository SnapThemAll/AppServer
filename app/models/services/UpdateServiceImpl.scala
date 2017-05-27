package models.services

import com.google.inject.Inject
import models.daos.UpdateDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class UpdateServiceImpl @Inject()(updateDAO: UpdateDAO) extends UpdateService {

  override def userNeedToUpdateApp(version: String): Future[Boolean] =
    updateDAO.find().map( optUpdate =>
      optUpdate.exists(update => update.serverVersion != version)
    )

  override def serverIsUpdating(): Future[Boolean] =
    updateDAO.find().map( optUpdate =>
      optUpdate.exists(update => update.serverIsUpdating)
    )
}
