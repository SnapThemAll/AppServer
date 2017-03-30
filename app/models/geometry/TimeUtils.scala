package models.geometry


object TimeUtils {

  def millisToHours(millis: Long): Double = millis/3600000d
  def millisToMinutes(millis: Long): Double = millis/60000d
}
