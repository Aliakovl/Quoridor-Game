import Dependencies._

lazy val `quoridor-game` = (project in file("."))
  .settings(
    name := "quoridor-game",
    version := "0.1.0",
    scalaVersion := "3.3.0",
    Compile / mainClass := Some("ru.quoridor.app.QuoridorApp"),
    dockerBaseImage := "quoridor-runtime:latest",
    dockerUpdateLatest := true,
    dockerExposedPorts := Seq(8080),
    bashScriptConfigLocation := Some("/conf/application.ini"),
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-Werror",
    ),
    libraryDependencies ++= dependencies,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    Compile / doc / sources := Nil
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)
