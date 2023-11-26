package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.GameApiService
import dev.aliakovl.quoridor.api.data.ExceptionResponse
import dev.aliakovl.quoridor.api.data.Requests.{
  PawnMoveRequest,
  PlaceWallRequest
}
import dev.aliakovl.quoridor.api.data.Responses.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import dev.aliakovl.quoridor.auth.AuthorizationService
import dev.aliakovl.quoridor.auth.AuthorizationService.validate
import dev.aliakovl.quoridor.auth.model.{AccessToken, Username}
import dev.aliakovl.quoridor.codec.circe.Orientation.given
import dev.aliakovl.quoridor.codec.circe.Side.given
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.given
import sttp.model.StatusCode
import sttp.tapir.CodecFormat

object GameAPI:
  def apply[
      Env <: GameApiService with AuthorizationService
  ]: List[ZServerEndpoint[Env, Any]] = List(
    createGameEndpoint.widen[Env],
    joinPlayerEndpoint.widen[Env],
    startGameEndpoint.widen[Env],
    gameHistoryEndpoint.widen[Env],
    historyEndpoint.widen[Env],
    getGameEndpoint.widen[Env],
    getUserEndpoint.widen[Env],
    pawnMoves.widen[Env],
    wallMoves.widen[Env],
    pawnMove.widen[Env],
    placeWall.widen[Env]
  )

  private val baseEndpoint =
    endpoint
      .in("api" / "v1")
      .securityIn(auth.bearer[AccessToken]())
      .errorOut(jsonBody[ExceptionResponse] and statusCode)
      .mapErrorOut(er => new Throwable(er._1.errorMessage))(
        ExceptionResponse(_)
      )
      .zServerSecurityLogic { accessToken =>
        validate(accessToken)
      }

  private val createGameEndpoint =
    baseEndpoint.post
      .in("game" / "create")
      .out(jsonBody[ProtoGameResponse] and statusCode(StatusCode.Created))
      .serverLogic(claims => _ => GameApiService.createGame(claims))

  private val joinPlayerEndpoint =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId"))
      .in("join" / path[ID[User]]("userId"))
      .out(jsonBody[ProtoGameResponse])
      .serverLogic(GameApiService.joinPlayer)

  private val startGameEndpoint =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "start")
      .out(jsonBody[GameResponse] and statusCode(StatusCode.Created))
      .serverLogic(GameApiService.startGame)

  private val gameHistoryEndpoint =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId") / "history")
      .out(jsonBody[List[GameResponse]])
      .serverLogic(GameApiService.gameHistory)

  private val historyEndpoint =
    baseEndpoint.get
      .in("history")
      .out(jsonBody[List[GamePreViewResponse]])
      .serverLogic { claims => _ =>
        GameApiService.history(claims)
      }

  private val getGameEndpoint =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId"))
      .out(jsonBody[GameResponse])
      .serverLogic(GameApiService.getGame)

  private val getUserEndpoint =
    baseEndpoint.get
      .in("user" / path[Username]("username"))
      .out(jsonBody[UserResponse])
      .serverLogic(GameApiService.getUser)

  private val pawnMove =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "movePawn")
      .in(jsonBody[PawnMoveRequest])
      .serverLogic(GameApiService.pawnMove)

  private val placeWall =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "placeWall")
      .in(jsonBody[PlaceWallRequest])
      .serverLogic(GameApiService.placeWall)

  private val pawnMoves =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId") / "pawnMoves")
      .out(jsonBody[List[PawnPositionResponse]])
      .serverLogic(GameApiService.availablePawnMoves)

  private val wallMoves =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId") / "wallMoves")
      .out(jsonBody[Set[WallPositionResponse]])
      .serverLogic(GameApiService.availableWallMoves)
