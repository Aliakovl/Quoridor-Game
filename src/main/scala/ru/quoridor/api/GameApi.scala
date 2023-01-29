package ru.quoridor.api

import ru.utils.Typed.Implicits._
import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.game.{Game, Move}
import ru.quoridor.services._
import ru.quoridor._
import sttp.model.StatusCode

import java.util.UUID

class GameApi(
    userService: UserService,
    gameCreator: GameCreator,
    gameService: GameService
) extends TapirApi {

  private val en = endpoint.in("api")

  private val createGameEndpoint: ZServerEndpoint[Any, Any] = en.post
    .in(path[UUID]("userId"))
    .in("create-game")
    .errorOut(jsonBody[ExceptionResponse])
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[ProtoGame])))
    .zServerLogic { uuid =>
      gameCreator.createGame(uuid.typed[User]).mapError(ExceptionResponse.apply)
    }

  private val joinPlayerEndpoint: ZServerEndpoint[Any, Any] = en.post
    .in(path[UUID]("userId"))
    .in("join-game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[ProtoGame])
    .zServerLogic { case (userId, gameId) =>
      gameCreator
        .joinPlayer(gameId.typed[Game], userId.typed[User])
        .mapError(ExceptionResponse.apply)
    }

  private val startGameEndpoint: ZServerEndpoint[Any, Any] = en.post
    .in(path[UUID]("userId"))
    .in("start-game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[Game])))
    .zServerLogic { case (userId, gameId) =>
      gameCreator
        .startGame(gameId.typed[Game], userId.typed[User])
        .mapError(ExceptionResponse.apply)
    }

  private val gameHistoryEndpoint: ZServerEndpoint[Any, Any] = en.get
    .in(path[UUID]("userId"))
    .in("game")
    .in("history")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[Game]])
    .zServerLogic { case (userId, gameId) =>
      gameService
        .gameHistory(gameId.typed[Game], userId.typed[User])
        .mapError(ExceptionResponse.apply)
    }

  private val historyEndpoint: ZServerEndpoint[Any, Any] = en.get
    .in(path[UUID]("userId"))
    .in("history")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[GamePreView]])
    .zServerLogic { userId =>
      userService
        .usersHistory(userId.typed[User])
        .mapError(ExceptionResponse.apply)
    }

  private val getGameEndpoint: ZServerEndpoint[Any, Any] = en.get
    .in(path[UUID]("userId"))
    .in("game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .zServerLogic { case (userId, gameId) =>
      gameService
        .findGame(gameId.typed[Game])
        .mapError(ExceptionResponse.apply)
    }

  private val moveEndpoint: ZServerEndpoint[Any, Any] = en.post
    .in(path[UUID]("userId"))
    .in("move")
    .in(query[UUID]("gameId"))
    .in(jsonBody[Move])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .zServerLogic { case (userId, gameId, move) =>
      gameService
        .makeMove(gameId.typed[Game], userId.typed[User], move)
        .mapError(ExceptionResponse.apply)
    }

  override val api =
    List(
      createGameEndpoint,
      joinPlayerEndpoint,
      startGameEndpoint,
      gameHistoryEndpoint,
      historyEndpoint,
      getGameEndpoint,
      moveEndpoint
    )
}
