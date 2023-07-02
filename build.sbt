import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "2.13.10"

lazy val circeVersion = "0.14.5"
lazy val tapirVersion = "1.2.10"
lazy val http4sVersion = "0.23.18"
lazy val zioVersion = "2.0.10"
lazy val zioLoggingVersion = "2.1.11"

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.9.0",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs" % "1.2.10",
  "com.softwaremill.sttp.apispec" %% "asyncapi-circe-yaml" % "0.3.2",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "com.beachape" %% "enumeratum" % "1.7.2",
  "com.beachape" %% "enumeratum-circe" % "1.7.2",
  "org.http4s" %% "http4s-blaze-server" % "0.23.14",
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-nio" % "2.0.1",
  "dev.zio" %% "zio-interop-cats" % "23.0.0.1",
  "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
  "org.postgresql" % "postgresql" % "42.5.4",
  "dev.zio" %% "zio-logging" % zioLoggingVersion,
  "dev.zio" %% "zio-logging-jpl" % zioLoggingVersion,
  "dev.zio" %% "zio-logging-slf4j2-bridge" % zioLoggingVersion,
  "dev.zio" %% "zio-config" % "4.0.0-RC12",
  "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC12",
  "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC12",
  "com.github.jwt-scala" %% "jwt-circe" % "9.2.0",
  "io.lettuce" % "lettuce-core" % "6.2.3.RELEASE",
  "com.password4j" % "password4j" % "1.7.0"
)

lazy val root = (project in file("."))
  .settings(
    name := "Quoridor Game",
    version := "0.1.0",
    Compile / mainClass := Some("ru.quoridor.app.QuoridorApp"),
    dockerBaseImage := "quoridor-runtime:latest",
    dockerUpdateLatest := true,
    dockerExposedPorts := Seq(8080),
    bashScriptConfigLocation := Some("/conf/application.ini"),
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-feature",
      "-unchecked",
      "-Werror",
      "-Xlint",
      "-Xlint:-byname-implicit"
    ),
    Compile / doc / sources := Nil
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)
