package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.auth.model.{AccessToken, ClaimData, Username}
import dev.aliakovl.quoridor.codec.json.given
import dev.aliakovl.quoridor.engine.game.Move
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, WallPosition}
import dev.aliakovl.quoridor.model
import dev.aliakovl.quoridor.model.{Game, GamePreView, ProtoGame, User}
import dev.aliakovl.quoridor.services.model.GameResponse
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.given
import io.circe.{Decoder, Encoder}
import sttp.tapir.ztapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.CodecFormat
import zio.{URLayer, ZLayer}

class GameEndpoints(base: BaseEndpoints):
  val createGameEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Unit,
    ErrorResponse,
    ProtoGame,
    Any
  ] =
    base.secureEndpoint.post
      .tag("Game creation")
      .name("create-game")
      .summary("Create a new game")
      .in("api" / "v1")
      .in("game" / "create")
      .out(jsonBody[ProtoGame] and statusCode(StatusCode.Created))

  val joinPlayerEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    (ID[Game], ID[User]),
    ErrorResponse,
    ProtoGame,
    Any
  ] =
    base.secureEndpoint.post
      .tag("Game creation")
      .name("join-player")
      .summary("Join the other user to the game")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId"))
      .in("join" / path[ID[User]]("userId"))
      .out(jsonBody[ProtoGame])

  val startGameEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    ID[Game],
    ErrorResponse,
    GameResponse,
    Any
  ] =
    base.secureEndpoint.post
      .tag("Game creation")
      .name("start-game")
      .summary("Start the game")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "start")
      .out(jsonBody[GameResponse] and statusCode(StatusCode.Created))

  val gameHistoryEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    ID[Game],
    ErrorResponse,
    List[Game],
    Any
  ] =
    base.secureEndpoint.get
      .tag("Game")
      .name("game-history")
      .summary("Step-by-step history of the game")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "history")
      .out(jsonBody[List[Game]])

  val historyEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Unit,
    ErrorResponse,
    List[GamePreView],
    Any
  ] =
    base.secureEndpoint.get
      .tag("User info")
      .name("user-history")
      .summary("All games with user participation")
      .in("api" / "v1")
      .in("history")
      .out(jsonBody[List[GamePreView]])

  val getGameEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    ID[Game],
    ErrorResponse,
    Game,
    Any
  ] =
    base.secureEndpoint.get
      .tag("Game")
      .name("game")
      .summary("Current game state")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId"))
      .out(jsonBody[Game])

  val getUserEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    Username,
    ErrorResponse,
    User,
    Any
  ] =
    base.secureEndpoint.get
      .tag("User info")
      .name("user-info")
      .summary("User profile info")
      .in("api" / "v1")
      .in("user" / path[Username]("username"))
      .out(jsonBody[User])

  val pawnMoveEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    (ID[Game], Move.PawnMove),
    ErrorResponse,
    Unit,
    Any
  ] =
    base.secureEndpoint.post
      .tag("Game")
      .name("pawn-move")
      .summary("The user makes a pawn move in the game")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "movePawn")
      .in(jsonBody[Move.PawnMove])

  val placeWallEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    (ID[Game], Move.PlaceWall),
    ErrorResponse,
    Unit,
    Any
  ] =
    base.secureEndpoint.post
      .tag("Game")
      .name("place-wall")
      .summary("The user places a wall in the game")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "placeWall")
      .in(jsonBody[Move.PlaceWall])

  val possiblePawnMovesEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    ID[Game],
    ErrorResponse,
    List[PawnPosition],
    Any
  ] =
    base.secureEndpoint.get
      .tag("Game")
      .name("possible-pawn-moves")
      .summary("Possible moves of the user's pawn in this turn")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "pawnMoves")
      .out(jsonBody[List[PawnPosition]])

  val possibleWallMovesEndpoint: ZPartialServerEndpoint[
    Any,
    AccessToken,
    ClaimData,
    ID[Game],
    ErrorResponse,
    Set[WallPosition],
    Any
  ] =
    base.secureEndpoint.get
      .tag("Game")
      .name("possible-wall-moves")
      .summary("Possible places to place the user's wall this turn")
      .in("api" / "v1")
      .in("game" / path[ID[Game]]("gameId") / "wallMoves")
      .out(jsonBody[Set[WallPosition]])

object GameEndpoints:
  val live: URLayer[BaseEndpoints, GameEndpoints] =
    ZLayer.fromFunction(new GameEndpoints(_))
