ThisBuild / scalaVersion := "2.13.10"

val catsVersion = "2.10.0"
val circeVersion = "0.14.6"
val jwtCirceVersion = "9.4.4"
val tapirVersion = "1.7.4"
val http4sVersion = "0.23.23"
val http4sBlazeVersion = "0.23.15"
val zioVersion = "2.0.17"
val zioLoggingVersion = "2.1.14"
val apispecVersion = "0.6.0"
val enumeratumVersion = "1.7.2"
val zioConfigVersion = "4.0.0-RC16"
val lettuceVersion = "6.2.6.RELEASE"
val password4jVersion = "1.7.3"
val postgresqlVersion = "42.6.0"
val quillVersion = "4.6.0"
val zioInteropCatsVersion = "23.0.0.8"
val zioNioVersion = "2.0.2"

val cats = Seq(
  "org.typelevel" %% "cats-core" % catsVersion
)

val tapir = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs" % tapirVersion,
  "com.softwaremill.sttp.apispec" %% "asyncapi-circe-yaml" % apispecVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion,
)

val server = Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion
)

val zio = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-nio" % zioNioVersion,
  "dev.zio" %% "zio-interop-cats" % zioInteropCatsVersion
)

val circe = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

val config = Seq(
  "dev.zio" %% "zio-config" % zioConfigVersion,
  "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
  "dev.zio" %% "zio-config-magnolia" % zioConfigVersion
)

val security = Seq(
  "com.password4j" % "password4j" % password4jVersion,
  "com.github.jwt-scala" %% "jwt-circe" % jwtCirceVersion
)

val database = Seq(
  "org.postgresql" % "postgresql" % postgresqlVersion,
  "io.getquill" %% "quill-jdbc-zio" % quillVersion
)

val redis = Seq(
  "io.lettuce" % "lettuce-core" % lettuceVersion
)

val enumeratum = Seq(
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumVersion
)

val logging = Seq(
  "dev.zio" %% "zio-logging" % zioLoggingVersion,
  "dev.zio" %% "zio-logging-jpl" % zioLoggingVersion,
  "dev.zio" %% "zio-logging-slf4j2-bridge" % zioLoggingVersion,
)

lazy val `quoridor-game` = (project in file("."))
  .settings(
    name := "quoridor-game",
    version := "0.1.0",
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
      "-Xlint",
      "-Xlint:-byname-implicit"
    ),
    libraryDependencies ++= cats ++ tapir ++ server ++ zio ++ circe ++ config ++
      security ++ database ++ redis ++ enumeratum ++ logging,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    Compile / doc / sources := Nil
  )
  .enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)
