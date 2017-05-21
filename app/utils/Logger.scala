package utils

/**
  * Implement this to get a named logger in scope.
  */
trait Logger {

  /**
    * A named logger instance.
    */
  private val logger = play.api.Logger(this.getClass).logger

  def error(msg: String): Unit = println("Error: " + msg)
  def error(msg: String, t: Throwable): Unit = {
    println("Error: " + msg)
    println("\t" + t.getMessage)
  }
  def log(msg: String): Unit = println("Log: " + msg)
}
