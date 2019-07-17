package statistician.zio.io

import scalaz.zio._

trait Logging{
  val logging: Logging.Service
}

object Logging {
  trait Service {
    def debug(msg: String): UIO[Unit]
    def info(msg: String): UIO[Unit]
    def warning(msg: String): UIO[Unit]
    def error(msg: String): UIO[Unit]
    def error(msg: String, error: Throwable): UIO[Unit]
  }

  trait Console extends Logging {
    val logging: Logging.Service = new Logging.Service {
      def debug(msg: String): UIO[Unit] =
      UIO.effectTotal(println(s"[DEBUG] $msg"))
      def info(msg: String): UIO[Unit] =
        UIO.effectTotal(println(s"[INFO] $msg"))
      def warning(msg: String): UIO[Unit] =
        UIO.effectTotal(println(s"[WARNING] $msg"))
      def error(msg: String): UIO[Unit] =
        UIO.effectTotal(println(s"[ERROR] $msg"))
      def error(msg: String, error: Throwable): UIO[Unit] =
        UIO.effectTotal(println(s"[ERROR] $msg. Error $error"))
    }
  }

  object Console extends Console

  def debug(msg: String): ZIO[Logging, Nothing, Unit]  = ZIO.accessM(_.logging debug msg)
  def info(msg: String): ZIO[Logging, Nothing, Unit]  = ZIO.accessM(_.logging info msg)
  def warn(msg: String): ZIO[Logging, Nothing, Unit]  = ZIO.accessM(_.logging warning  msg)
  def error(msg: String): ZIO[Logging, Nothing, Unit]  = ZIO.accessM(_.logging error  msg)
  def error(msg: String, error: Throwable): ZIO[Logging, Nothing, Unit]  = ZIO.accessM(_.logging error(msg, error))
}