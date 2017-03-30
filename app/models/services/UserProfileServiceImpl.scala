package models.services

import java.util.UUID
import javax.inject.Inject

import models.daos.TrackDAO
import models.profile.UserProfile

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by cleme on 14-Dec-16.
  */
class UserProfileServiceImpl @Inject()(trackDAO: TrackDAO) extends UserProfileService {

  override def compute(userID: UUID): Future[UserProfile] = trackDAO.findAll(userID).map(UserProfile(_))
}
