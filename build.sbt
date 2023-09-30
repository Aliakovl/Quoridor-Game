import Dependencies.*
import com.typesafe.sbt.packager.docker.*

lazy val `quoridor-game` = (project in file("."))
  .settings(
    name := "game-api",
    version := "0.1.0",
    scalaVersion := "3.3.1",
    Compile / mainClass := Some("ru.quoridor.app.QuoridorApp"),
    dockerBaseImage := "quoridor/runtime:latest",
    dockerExposedPorts := Seq(8080),
    dockerUsername := Some("quoridor"),
    daemonUserUid := Some("1001"),
    daemonGroupGid := Some("0"),
    version := "latest",
    dockerApiVersion := Some(DockerApiVersion(1, 43)),
    bashScriptConfigLocation := Some("/conf/application.ini"),
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-Werror"
    ),
    libraryDependencies ++= dependencies,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    Compile / doc / sources := Nil
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)
