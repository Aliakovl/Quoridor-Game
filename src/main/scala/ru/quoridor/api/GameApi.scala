package ru.quoridor.api

import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.model.{GamePreView, ProtoGame}
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.services.GameCreator.{createGame, joinPlayer, startGame}
import ru.quoridor.services.GameService.{findGame, gameHistory, makeMove}
import ru.quoridor.services.UserService.usersHistory
import sttp.model.StatusCode

import java.util.UUID

object GameApi {

  private val en = endpoint.in("api")

  val createGameEndpoint: ZServerEndpoint[GameCreator, Any] = en.post
    .in(path[UUID]("userId"))
    .in("create-game")
    .errorOut(jsonBody[ExceptionResponse])
    .out(statusCode(StatusCode.Created).and(jsonBody[ProtoGame]))
    .zServerLogic { uuid =>
      createGame(uuid).mapError(ExceptionResponse.apply)
    }

  val joinPlayerEndpoint: ZServerEndpoint[GameCreator, Any] = en.post
    .in(path[UUID]("userId"))
    .in("join-game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[ProtoGame])
    .zServerLogic { case (userId, gameId) =>
      joinPlayer(gameId, userId)
        .mapError(ExceptionResponse.apply)
    }

  val startGameEndpoint: ZServerEndpoint[GameCreator, Any] = en.post
    .in(path[UUID]("userId"))
    .in("start-game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(statusCode(StatusCode.Created).and(jsonBody[Game]))
    .zServerLogic { case (userId, gameId) =>
      startGame(gameId, userId)
        .mapError(ExceptionResponse.apply)
    }

  val gameHistoryEndpoint: ZServerEndpoint[GameService, Any] = en.get
    .in(path[UUID]("userId"))
    .in("game")
    .in("history")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[Game]])
    .zServerLogic { case (userId, gameId) =>
      gameHistory(gameId, userId)
        .mapError(ExceptionResponse.apply)
    }

  val historyEndpoint: ZServerEndpoint[UserService, Any] = en.get
    .in(path[UUID]("userId"))
    .in("history")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[GamePreView]])
    .zServerLogic { userId =>
      usersHistory(userId)
        .mapError(ExceptionResponse.apply)
    }

  val getGameEndpoint: ZServerEndpoint[GameService, Any] = en.get
    .in(path[UUID]("userId"))
    .in("game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .zServerLogic { case (userId, gameId) =>
      findGame(gameId)
        .mapError(ExceptionResponse.apply)
    }

  val moveEndpoint: ZServerEndpoint[GameService, Any] = en.post
    .in(path[UUID]("userId"))
    .in("move")
    .in(query[UUID]("gameId"))
    .in(jsonBody[Move])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .zServerLogic { case (userId, gameId, move) =>
      makeMove(gameId, userId, move)
        .mapError(ExceptionResponse.apply)
    }

}
