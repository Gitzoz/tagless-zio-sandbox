package statistician.zio.io

import java.io.{File, IOException}

import cats.data.ValidatedNel
import scalaz.zio._
import cats.implicits._

trait FileSystem {
 val fileSystem: FileSystem.Service
}

object FileSystem {
  case class FileSystemError(error: String) extends IOException

  trait Service {
    def getFiles(dir: String): IO[FileSystemError, List[File]]
    def getDirectories(dir: String): IO[FileSystemError, List[File]]
    def searchForType(typ: String, dir: String): IO[List[FileSystemError], List[File]]
  }

  trait Live extends FileSystem {
    val fileSystem: FileSystem.Service = new FileSystem.Service {
      def getFiles(dir: String): IO[FileSystemError, List[File]] =
        ZIO.fromEither {
          val d = new File(dir)
          if(d.exists && d.isDirectory)
            d.listFiles(_.isFile).toList.asRight
          else
            FileSystemError(s"$dir was no directory").asLeft
        }

      def getDirectories(dir: String): IO[FileSystemError, List[File]] =
        ZIO.fromEither {
          val d = new File(dir)
          if(d.exists && d.isDirectory)
            d.listFiles(_.isDirectory).toList.asRight
          else
            FileSystemError(s"$dir was no directory").asLeft
        }

      def searchForType(typ: String, dir: String): IO[List[FileSystemError], List[File]] = {
        def combine(a: IO[List[FileSystemError], List[File]], b: IO[List[FileSystemError], List[File]]) =
          ZIO.absolve {
            for {
              validatedA <- a.either.map(_.toValidated)
              validatedB <- b.either.map(_.toValidated)
              combined = (validatedA combine validatedB).toEither
            } yield combined
          }

        def helper(files: IO[List[FileSystemError], List[File]], directories: IO[List[FileSystemError], List[File]]): IO[List[FileSystemError], List[File]]  = {
          for {
            dirFiles <- directories
            result <- if(dirFiles.isEmpty)
              files
            else {
              dirFiles.map(_.getAbsolutePath).map { path =>
                val combinedFiles = combine(files, getFiles(path).mapError(List(_)))
                helper(combinedFiles, getDirectories(path).mapError(List(_)))
              }.reduce(combine)
            }

          } yield result
        }

        helper(getFiles(dir).mapError(List(_)), getDirectories(dir).mapError(List(_))).map(_.filter(_.getName contains typ))
      }
    }

  }

  object Live extends Live

  def getFiles(dir: String): ZIO[FileSystem, FileSystemError, List[File]] =
    ZIO.accessM(_.fileSystem getFiles dir)

  def getDirectories(dir: String): ZIO[FileSystem, FileSystemError, List[File]] =
    ZIO.accessM(_.fileSystem getDirectories dir)

  def searchForType(typ: String, dir: String): ZIO[FileSystem, List[FileSystemError], List[File]] =
    ZIO.accessM(_.fileSystem searchForType (typ, dir))

}
