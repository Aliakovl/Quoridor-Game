package ru.quoridor.api

import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.model.{GamePreView, ProtoGame, User}
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.services.{GameCreator, GameService}
import ru.quoridor.services.GameCreator._
import ru.quoridor.services.GameService._
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged._
import sttp.model.StatusCode

object GameApi {

  private val baseEndpoint =
    endpoint.in("api").errorOut(jsonBody[ExceptionResponse])

  val createGameEndpoint: ZServerEndpoint[GameCreator, Any] = baseEndpoint.post
    .in(path[ID[User]]("userId") / "create-game")
    .out(jsonBody[ProtoGame] and statusCode(StatusCode.Created))
    .zServerLogic { uuid =>
      createGame(uuid).mapError(ExceptionResponse.apply)
    }

  val joinPlayerEndpoint: ZServerEndpoint[GameCreator, Any] = baseEndpoint.post
    .in(path[ID[User]]("userId") / "join-game" / query[ID[Game]]("gameId"))
    .out(jsonBody[ProtoGame])
    .zServerLogic { case (userId, gameId) =>
      joinPlayer(gameId, userId)
        .mapError(ExceptionResponse.apply)
    }

  val startGameEndpoint: ZServerEndpoint[GameCreator, Any] = baseEndpoint.post
    .in(path[ID[User]]("userId") / "start-game" / query[ID[Game]]("gameId"))
    .out(jsonBody[Game] and statusCode(StatusCode.Created))
    .zServerLogic { case (userId, gameId) =>
      startGame(gameId, userId)
        .mapError(ExceptionResponse.apply)
    }

  val gameHistoryEndpoint: ZServerEndpoint[GameService, Any] = baseEndpoint.get
    .in(
      path[ID[User]]("userId") / "game" / "history" / query[ID[Game]]("gameId")
    )
    .out(jsonBody[List[Game]])
    .zServerLogic { case (userId, gameId) =>
      gameHistory(gameId, userId)
        .mapError(ExceptionResponse.apply)
    }

  val historyEndpoint: ZServerEndpoint[GameService, Any] = baseEndpoint.get
    .in(path[ID[User]]("userId") / "history")
    .out(jsonBody[List[GamePreView]])
    .zServerLogic { userId =>
      usersHistory(userId)
        .mapError(ExceptionResponse.apply)
    }

  val getGameEndpoint: ZServerEndpoint[GameService, Any] = baseEndpoint.get
    .in(path[ID[User]]("userId") / "game" / query[ID[Game]]("gameId"))
    .out(jsonBody[Game])
    .zServerLogic { case (userId, gameId) =>
      findGame(gameId)
        .mapError(ExceptionResponse.apply)
    }

  val moveEndpoint: ZServerEndpoint[GameService, Any] = baseEndpoint.post
    .in(path[ID[User]]("userId") / "move" / query[ID[Game]]("gameId"))
    .in(jsonBody[Move])
    .out(jsonBody[Game])
    .zServerLogic { case (userId, gameId, move) =>
      makeMove(gameId, userId, move)
        .mapError(ExceptionResponse.apply)
    }

}
