package ru.quoridor.api

import io.circe.{Decoder, Encoder}
import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import io.circe.generic.auto.*
import ru.quoridor.auth.model.{AccessToken, ClaimData, Username}
import ru.quoridor.model
import ru.quoridor.model.game.geometry.{PawnPosition, WallPosition}
import ru.quoridor.model.{GamePreView, ProtoGame, User, game}
import ru.quoridor.model.game.{Game, Move}
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.given
import sttp.model.StatusCode
import sttp.tapir.CodecFormat
import zio.ZLayer

class GameEndpoints(base: BaseEndpoints):
  val createGameEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Unit,
    Throwable,
    ProtoGame,
    Any
  ] =
    base.secureEndpoint.post
      .in("api" / "v1")
      .in("game" / "create")
      .out(jsonBody[ProtoGame] and statusCode(StatusCode.Created))

  val joinPlayerEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    (ID[Game], ID[User]),
    Throwable,
    ProtoGame,
    Any
  ] =
    base.secureEndpoint.post
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId"))
      .in("join" / path[ID[User]]("userId"))
      .out(jsonBody[ProtoGame])

  val startGameEndpoint: ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
    Game
  ], Throwable, Game, Any] =
    base.secureEndpoint.post
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "start")
      .out(jsonBody[Game] and statusCode(StatusCode.Created))

  val gameHistoryEndpoint
      : ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
        Game
      ], Throwable, List[Game], Any] =
    base.secureEndpoint.get
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "history")
      .out(jsonBody[List[Game]])

  val historyEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Unit,
    Throwable,
    List[GamePreView],
    Any
  ] =
    base.secureEndpoint.get
      .in("api" / "v1")
      .in("history")
      .out(jsonBody[List[GamePreView]])

  val getGameEndpoint: ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
    Game
  ], Throwable, Game, Any] =
    base.secureEndpoint.get
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId"))
      .out(jsonBody[Game])

  val getUserEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Username,
    Throwable,
    User,
    Any
  ] =
    base.secureEndpoint.get
      .in("api" / "v1")
      .in("user" / path[Username]("username"))
      .out(jsonBody[User])

  val pawnMoveEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    (ID[Game], game.Move.PawnMove),
    Throwable,
    Unit,
    Any
  ] =
    base.secureEndpoint.post
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "movePawn")
      .in(jsonBody[Move.PawnMove])

  val placeWallEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    (ID[Game], model.game.Move.PlaceWall),
    Throwable,
    Unit,
    Any
  ] =
    base.secureEndpoint.post
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "placeWall")
      .in(jsonBody[Move.PlaceWall])

  val pawnMovesEndpoint: ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
    Game
  ], Throwable, List[PawnPosition], Any] =
    base.secureEndpoint.get
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "pawnMoves")
      .out(jsonBody[List[PawnPosition]])

  val wallMovesEndpoint: ZPartialServerEndpoint[Any, AccessToken, ClaimData, ID[
    Game
  ], Throwable, Set[WallPosition], Any] =
    base.secureEndpoint.get
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "wallMoves")
      .out(jsonBody[Set[WallPosition]])

object GameEndpoints:
  val live: ZLayer[BaseEndpoints, Nothing, GameEndpoints] =
    ZLayer.fromFunction(new GameEndpoints(_))
