package ru.quoridor.api

import io.circe.generic.auto._
import ru.quoridor.app.QuoridorGame.Env
import ru.quoridor.auth.AuthorizationService.validate
import ru.quoridor.auth.model.AccessToken
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.services.GameService
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged._
import sttp.apispec.asyncapi.AsyncAPI
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.CodecFormat
import sttp.tapir.docs.asyncapi.AsyncAPIInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.stream.ZStream
import zio.{Hub, ZIO}

object GameAsyncAPI {
  private val wsEndpoint = endpoint.get
    .in("game" / path[ID[Game]]("gameId"))
    .securityIn(auth.bearer[AccessToken]())
    .errorOut(jsonBody[ExceptionResponse] and statusCode)
    .mapErrorOut(er => new Throwable(er._1.errorMessage))(ExceptionResponse(_))
    .out(
      webSocketBody[Move, CodecFormat.Json, Game, CodecFormat.Json](
        ZioStreams
      )
    )

  val docs: AsyncAPI =
    AsyncAPIInterpreter().toAsyncAPI(wsEndpoint, "Game Async Api", "0.0.1")

  private val wsLogic = wsEndpoint
    .zServerSecurityLogic { accessToken =>
      validate(accessToken)
    }
    .serverLogic { claimData => gameId =>
      ZIO.serviceWithZIO[GameService] { gameService =>
        ZIO.service[Hub[Game]].map { hub => in =>
          ZStream.fromZIO(gameService.findGame(gameId)) ++ ZStream
            .fromHub(hub)
            .filter(_.id == gameId)
            .mergeLeft(in.mapZIO { move =>
              gameService
                .makeMove(gameId, claimData.userId, move)
                .foldZIO(_ => ZIO.succeed(false), hub.publish)
            })
        }
      }
    }

  val wsRoute =
    ZHttp4sServerInterpreter().fromWebSocket(wsLogic.widen[Env]).toRoutes
}
