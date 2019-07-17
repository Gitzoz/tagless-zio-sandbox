import sbt._

object Versions {
  val scalaTest = "3.0.5"
  val zio = "1.0-RC4"
  val fs2 = "1.0.4"
}

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val zio = "org.scalaz" %% "scalaz-zio" % Versions.zio
  lazy val fs2Core = "co.fs2" %% "fs2-core" % Versions.fs2
  lazy val fs2Io = "co.fs2" %% "fs2-io" % Versions.fs2
  lazy val cats = "org.typelevel" %% "cats-core" % "1.6.0"
}
