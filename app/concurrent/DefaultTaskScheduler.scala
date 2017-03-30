package concurrent

import java.util.concurrent.{ForkJoinTask, ForkJoinWorkerThread, RecursiveTask}

import concurrent.Parallel.forkJoinPool

private[concurrent] class DefaultTaskScheduler extends TaskScheduler {

  def schedule[T](body: => T): ForkJoinTask[T] = {
    val t = new RecursiveTask[T] {
      def compute = body
    }
    Thread.currentThread match {
      case _: ForkJoinWorkerThread =>
        t.fork()
      case _ =>
        forkJoinPool.execute(t)
    }
    t
  }
}
