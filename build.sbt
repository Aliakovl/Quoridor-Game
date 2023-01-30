
ThisBuild / scalaVersion := "2.13.8"

lazy val circeVersion = "0.14.3"
lazy val tapirVersion = "1.2.7"
lazy val http4sVersion = "0.23.18"
lazy val doobieVersion = "1.0.0-RC1"
lazy val zioVersion = "2.0.6"

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.9.0",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "com.beachape" %% "enumeratum" % "1.7.2",
  "com.beachape" %% "enumeratum-circe" % "1.7.2",
  "org.http4s" %% "http4s-blaze-server" % "0.23.13",
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.reactormonk" %% "cryptobits" % "1.3.1",
  "com.github.pureconfig" %% "pureconfig" % "0.17.2",
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-interop-cats" % "23.0.0.1",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "Quoridor Game",
    scalacOptions += "-feature"
  )

