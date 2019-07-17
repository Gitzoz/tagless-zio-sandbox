package statistician.tagless.io

import java.io.File

import cats.effect.IO
import cats.implicits._
import FileSystem.FileSystemError

trait FileSystem[F[_]] {
  def getFiles(dir: String): F[Either[FileSystemError, Seq[File]]]
  def getDirectories(dir: String): F[Either[FileSystemError, Seq[File]]]
}

object FileSystem {
  case class FileSystemError(error: String)


 implicit def ioFileSystem = new FileSystem[IO] {
   override def getFiles(dir: String): IO[Either[FileSystemError, Seq[File]]] = IO {
     val d = new File(dir)
     if(d.exists && d.isDirectory)
       d.listFiles(_.isFile).toSeq.asRight
     else
       FileSystemError(s"$dir was no directory").asLeft
   }

   override def getDirectories(dir: String): IO[Either[FileSystemError, Seq[File]]] = IO {
     val d = new File(dir)
     if(d.exists && d.isDirectory)
       d.listFiles(_.isDirectory).toSeq.asRight
     else
       FileSystemError(s"$dir was no directory").asLeft
   }
 }
}
