package ru.quoridor.app

import io.circe.generic.auto._
import org.http4s.HttpRoutes
import pureconfig._
import pureconfig.generic.auto._
import ru.quoridor.api.ExceptionResponse
import ru.quoridor.api.GameApi._
import ru.quoridor.api.UserApi._
import ru.quoridor.services.{GameCreator, GameService, UserService}
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.statusCode
import sttp.tapir.ztapir._
import zio.{RIO, ZLayer}
import zio.interop.catz._

object QuoridorGame {

  val appConfigLayer: ZLayer[Any, Nothing, AppConfig] =
    ZLayer.fromFunction(() => ConfigSource.default.loadOrThrow[AppConfig])

  type Env = GameService with GameCreator with UserService
  type EnvTask[A] = RIO[Env, A]

  val api: List[ZServerEndpoint[Env, Any]] = List(
    createUser.widen[Env],
    loginUser.widen[Env],
    getUser.widen[Env],
    createGameEndpoint.widen[Env],
    joinPlayerEndpoint.widen[Env],
    startGameEndpoint.widen[Env],
    gameHistoryEndpoint.widen[Env],
    historyEndpoint.widen[Env],
    getGameEndpoint.widen[Env],
    moveEndpoint.widen[Env]
  )

  private val serverOptions = Http4sServerOptions
    .customiseInterceptors[EnvTask]
    .exceptionHandler(ExceptionHandler.pure[EnvTask] { ctx =>
      Some(
        ValuedEndpointOutput(
          jsonBody[ExceptionResponse],
          ExceptionResponse(ctx.e)
        ).prepend(statusCode, ExceptionResponse.exceptionCode(ctx.e))
      )
    })
    .options

  val apiRoutes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter(serverOptions).from(api).toRoutes
}
