package concurrent

import java.util.concurrent.ForkJoinTask

import concurrent.Parallel.task

private[concurrent] abstract class TaskScheduler {

  def schedule[T](body: => T): ForkJoinTask[T]

  def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
    val right = task {
      taskB
    }
    val left = taskA
    (left, right.join())
  }
}
