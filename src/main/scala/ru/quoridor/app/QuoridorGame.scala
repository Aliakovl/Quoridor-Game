package ru.quoridor.app

import org.http4s.HttpRoutes
import ru.quoridor.api.GameApi._
import ru.quoridor.api.Authorization._
import ru.quoridor.auth.{AuthenticationService, AuthorizationService}
import ru.quoridor.services.{GameCreator, GameService, UserService}
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.RIO

object QuoridorGame {
  type Env = GameService
    with GameCreator
    with UserService
    with AuthenticationService
    with AuthorizationService

  type EnvTask[A] = RIO[Env, A]

  val api: List[ZServerEndpoint[Env, Any]] = List(
    singOnEndpoint.widen[Env],
    signInEndpoint.widen[Env],
    refreshEndpoint.widen[Env],
    signOutEndpoint.widen[Env],
    createGameEndpoint.widen[Env],
    joinPlayerEndpoint.widen[Env],
    startGameEndpoint.widen[Env],
    gameHistoryEndpoint.widen[Env],
    historyEndpoint.widen[Env],
    getGameEndpoint.widen[Env],
    moveEndpoint.widen[Env]
  )

  val apiRoutes: HttpRoutes[EnvTask] =
    ZHttp4sServerInterpreter().from(api).toRoutes
}
