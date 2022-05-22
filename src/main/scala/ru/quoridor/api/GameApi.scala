package ru.quoridor.api

import cats.effect.IO
import cats.implicits._
import ru.quoridor.ExceptionResponse._
import ru.utils.Typed.Implicits._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.game.{Game, Move}
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.{ExceptionResponse, GamePreView, ProtoGame, User}
import sttp.model.StatusCode

import java.util.UUID

class GameApi(userService: UserService[IO],
              gameCreator: GameCreator[IO],
              gameService: GameService[IO]) extends TapirApi {

  private val en = endpoint.in("api")

  private val createGameEndpoint = en.post
    .in(path[UUID]("userId"))
    .in("create-game")
    .errorOut(oneOf(
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[ProtoGame])))
    .serverLogic[IO] { uuid =>
      gameCreator.createGame(uuid.typed[User]).map(x => Right(x)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
    }

  private val joinPlayerEndpoint = en.post
    .in(path[UUID]("userId"))
    .in("join-game")
    .in(query[UUID]("gameId"))
    .errorOut(oneOf(
      oneOfVariant(StatusCode.BadRequest, jsonBody[ExceptionResponse400]),
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(jsonBody[ProtoGame])
    .serverLogic[IO] { case (userId, gameId) =>
      gameCreator.joinPlayer(gameId.typed[Game], userId.typed[User])
        .map(x => Right(x)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
    }

  private val startGameEndpoint = en.post
    .in(path[UUID]("userId"))
    .in("start-game")
    .in(query[UUID]("gameId"))
    .errorOut(oneOf(
      oneOfVariant(StatusCode.BadRequest, jsonBody[ExceptionResponse400]),
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.Forbidden, jsonBody[ExceptionResponse403]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(oneOf(oneOfVariant(StatusCode.Created, jsonBody[Game])))
    .serverLogic[IO] { case (userId, gameId) =>
      gameCreator.startGame(gameId.typed[Game], userId.typed[User])
        .map(x => Right(x)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
    }

  private val gameHistoryEndpoint = en.get
    .in(path[UUID]("userId"))
    .in("game")
    .in("history")
    .in(query[UUID]("gameId"))
    .errorOut(oneOf(
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.Forbidden, jsonBody[ExceptionResponse403]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(jsonBody[List[Game]])
    .serverLogic[IO] { case (userId, gameId) =>
      gameService.gameHistory(gameId.typed[Game], userId.typed[User])
        .map(x => Right(x)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
    }

  private val historyEndpoint = en.get
    .in(path[UUID]("userId"))
    .in("history")
    .errorOut(oneOf(
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(jsonBody[List[GamePreView]])
    .serverLogic[IO] { userId =>
      userService.usersHistory(userId.typed[User]).map(Right(_)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
    }

  private val getGameEndpoint = en.get
    .in(path[UUID]("userId"))
    .in("game")
    .in(query[UUID]("gameId"))
    .errorOut(oneOf(
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId) =>
      gameService.findGame(gameId.typed[Game], userId.typed[User]).map(Right(_)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
    }


  private val moveEndpoint = en.post
    .in(path[UUID]("userId"))
    .in("move")
    .in(query[UUID]("gameId"))
    .in(jsonBody[Move])
    .errorOut(oneOf(
      oneOfVariant(StatusCode.BadRequest, jsonBody[ExceptionResponse400]),
      oneOfVariant(StatusCode.Forbidden, jsonBody[ExceptionResponse403]),
      oneOfVariant(StatusCode.NotFound, jsonBody[ExceptionResponse404]),
      oneOfVariant(StatusCode.InternalServerError, jsonBody[ExceptionResponse500])
    ))
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId, move) =>
      gameService.makeMove(gameId.typed[Game], userId.typed[User], move)
        .map(x => Right(x)).handleError { er =>
        ExceptionResponse(er).asLeft
      }
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
