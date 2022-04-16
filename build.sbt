
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.7.0"
)

lazy val root = (project in file("."))
  .settings(
    name := "quoridor"
  )

