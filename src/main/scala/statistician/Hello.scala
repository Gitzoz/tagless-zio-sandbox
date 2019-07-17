package statistician

import java.io
import java.io.File

import cats.Monad
import cats.effect.IO
import cats.implicits._
import cats.data._
import scalaz.zio._
import statistician.tagless.io.{FileSystem, Logging}
import statistician.zio.io.{FileSystem => ZIOFileSystem, Logging => ZIOLogging}


object Hello extends scala.App {
  def programm[F[_]: Monad](implicit logger: Logging[F]) = {
    for {
      _ <- logger.debug("Test")
      _ <- logger.info("INFO")
      _ <- logger.warning("Warning")
      _ <- logger.error("Error")
      _ <- logger.error("Error", new Exception("Boom"))
    } yield ()
  }

  def listFiles[F[_]: Monad](dir: String)(implicit fileSystem: FileSystem[F], logger: Logging[F]) = {
    for {
      files <- fileSystem.getFiles(dir)
      directories <- fileSystem.getDirectories(dir)
      _ <- logger.info(s"Files: ${files.fold(error => error.error, _.map(_.getName))}")
      _ <- logger.info(s"Directories: ${directories.fold(error => error.error, _.map(_.getName))}")
      validatedFiles = Validated.fromEither(files).bimap(_.error, _.map(_.getName).toList).toValidatedNel
      validatedDirs = Validated.fromEither(directories).bimap(_.error, _.map(_.getName).toList).toValidatedNel
      foo = validatedFiles combine validatedDirs
      _ <- foo.fold(error => logger.error(error.toList.mkString(", ")), stuff => logger.info(stuff.mkString(", ")))
    } yield (foo)
  }


  def programmZio(dir: String) = {
    val files = ZIOFileSystem.getFiles(dir)
    val directories = ZIOFileSystem.getDirectories(dir)

    for {
      validatedFiles <- files.mapError(_.error).either.map(_.toValidatedNel)
      validatedDirs <- directories.mapError(_.error).either.map(_.toValidatedNel)
      failures = validatedFiles combine validatedDirs
      _  <- failures.fold(error => ZIOLogging.error(error.toList.mkString(", ")), stuff => ZIOLogging.info(stuff.mkString(", ")))
    } yield ()
  }

  def search(dir: String, typ: String): ZIO[ZIOLogging with ZIOFileSystem, Nothing, Unit] =
    ZIOFileSystem.searchForType(typ, dir).foldM(
      error => ZIOLogging.error(error.mkString(", ")),
      stuff => ZIOLogging.info(stuff.map(_.getName).mkString(", "))
    )


  //programm[IO].unsafeRunSync()

  //listFiles[IO]("./").unsafeRunSync()

  //val liveProgramm = programmZio("./")
  val searchProgramm = search("./../", ".scala").provide(LiveEnv)

  new DefaultRuntime{}.unsafeRun(searchProgramm)


}

object LiveEnv extends ZIOLogging with ZIOFileSystem {
  override val logging: ZIOLogging.Service = ZIOLogging.Console.logging
  override val fileSystem: ZIOFileSystem.Service = ZIOFileSystem.Live.fileSystem
}


