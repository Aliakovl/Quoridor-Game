import Versions.*
import sbt.*

object Dependencies {
  lazy val dependencies: Seq[ModuleID] =
    cats ++ tapir ++ server ++ zio ++ circe ++ config ++
      security ++ database ++ redis ++ enumeratum ++ logging

  private val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion
  )

  private val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs" % tapirVersion,
    "com.softwaremill.sttp.apispec" %% "asyncapi-circe-yaml" % apispecVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion
  )

  private val server = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion
  )

  private val zio = Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-streams" % zioVersion,
    "dev.zio" %% "zio-nio" % zioNioVersion,
    "dev.zio" %% "zio-interop-cats" % zioInteropCatsVersion
  )

  private val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )

  private val config = Seq(
    "dev.zio" %% "zio-config" % zioConfigVersion,
    "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
    "dev.zio" %% "zio-config-magnolia" % zioConfigVersion
  )

  private val security = Seq(
    "com.password4j" % "password4j" % password4jVersion,
    "com.github.jwt-scala" %% "jwt-circe" % jwtCirceVersion
  )

  private val database = Seq(
    "org.postgresql" % "postgresql" % postgresqlVersion,
    "io.getquill" %% "quill-jdbc-zio" % quillVersion
  )

  private val redis = Seq(
    "io.lettuce" % "lettuce-core" % lettuceVersion
  )

  private val enumeratum = Seq(
    "com.beachape" %% "enumeratum" % enumeratumVersion,
    "com.beachape" %% "enumeratum-circe" % enumeratumVersion
  )

  private val logging = Seq(
    "dev.zio" %% "zio-logging" % zioLoggingVersion,
    "dev.zio" %% "zio-logging-jpl" % zioLoggingVersion,
    "dev.zio" %% "zio-logging-slf4j2-bridge" % zioLoggingVersion
  )
}
