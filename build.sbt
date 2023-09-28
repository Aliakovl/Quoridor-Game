import Dependencies._
import com.typesafe.sbt.packager.docker.DockerApiVersion

lazy val `quoridor-game` = (project in file("."))
  .settings(
    name := "quoridor-game",
    version := "0.1.0",
    scalaVersion := "3.3.1",
    Compile / mainClass := Some("ru.quoridor.app.QuoridorApp"),
    Docker / dockerBaseImage := "quoridor-runtime:latest",
    Docker / dockerUpdateLatest := true,
    Docker / dockerExposedPorts := Seq(8080),
    Docker / dockerRepository := Some("quoridor.online:5000"),
    Docker / dockerUsername := Some("quoridor"),
    Docker / daemonUser := "quoridor",
    Docker / dockerAutoremoveMultiStageIntermediateImages := true,
    Docker / dockerApiVersion := Some(DockerApiVersion(1, 43)),
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
