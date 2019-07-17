package statistician.tagless.io

import cats.effect._

trait Logging[F[_]] {
  def debug(msg: String): F[Unit]
  def info(msg: String): F[Unit]
  def warning(msg: String): F[Unit]
  def error(msg: String): F[Unit]
  def error(msg: String, error: Throwable): F[Unit]
}

object Logging {
  implicit def ioLogging = new Logging[IO] {
    override def debug(msg: String): IO[Unit] = IO {
      println(s"[DEBUG] $msg")
    }

    override def info(msg: String): IO[Unit] = IO {
      println(s"[INFO] $msg")
    }

    override def warning(msg: String): IO[Unit] = IO {
      println(s"[WARNING] $msg")
    }

    override def error(msg: String): IO[Unit] = IO {
      println(s"[ERROR] $msg")
    }

    override def error(msg: String, error: Throwable): IO[Unit] = IO {
      println(s"[ERROR] $msg. Error $error")
    }
  }
}

