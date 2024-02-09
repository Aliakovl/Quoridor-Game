import Dependencies.*
import com.typesafe.sbt.packager.docker.*

lazy val `quoridor-game` = (project in file("."))
  .settings(
    name := "game-api",
    version := "0.1.0",
    scalaVersion := "3.3.1",
    Compile / mainClass := Some("dev.aliakovl.quoridor.app.QuoridorApp"),
    dockerBaseImage := "eclipse-temurin:17.0.8.1_1-jre",
    dockerExposedPorts := Seq(8080),
    dockerUsername := Some("quoridor"),
    Docker / daemonUserUid := Some("1001"),
    Docker / daemonGroupGid := Some("0"),
    version := "latest",
    dockerApiVersion := Some(DockerApiVersion(1, 43)),
    bashScriptConfigLocation := Some("/conf/application.ini"),
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-Werror",
      "-Wunused:all"
    ),
    libraryDependencies ++= dependencies,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    Compile / doc / sources := Nil
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)

lazy val tbsg = (project in file("tbsg"))
  .settings(
    name := "tbsg",
    description := "Turn-based strategy games",
    version := "0.0.1",
    scalaVersion := "3.3.1",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.10.0"
    )
  )
