package models.services

import java.util.UUID

import models.profile.UserProfile

import scala.concurrent.Future

trait UserProfileService {

  def compute(userID: UUID): Future[UserProfile]
}
