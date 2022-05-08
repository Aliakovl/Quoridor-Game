
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC1",
  "dev.zio" %% "zio" % "1.0.14"
)

lazy val root = (project in file("."))
  .settings(
    name := "quoridor"
  )

