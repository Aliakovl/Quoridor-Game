
ThisBuild / scalaVersion := "2.13.8"

lazy val circeVersion = "0.14.1"
lazy val tapirVersion = "0.20.1"
lazy val http4sVersion = "0.23.11"
lazy val doobieVersion = "1.0.0-RC1"

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "com.beachape" %% "enumeratum" % "1.7.0",
  "com.beachape" %% "enumeratum-circe" % "1.7.0",
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.reactormonk" %% "cryptobits" % "1.3.1"
)

lazy val root = (project in file("."))
  .settings(
    name := "Quoridor Game"
  )

