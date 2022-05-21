package model.api

import cats.effect.IO
import model.services.{GameCreator, GameService, UserService}
import model.ExceptionResponse
import model.ProtoGame
import model.User
import model.game.{Game, Move, PawnMove, PlaceWall}
import model.game.geometry.{PawnPosition, WallPosition}
import model.GamePreView
import utils.Typed.Implicits._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
//import fs2._
//import fs2.concurrent.Topic
//import sttp.capabilities.WebSockets
//import sttp.capabilities.fs2.Fs2Streams
//import sttp.ws.{WebSocket, WebSocketFrame}
//import utils.Typed.ID
//import utils.Typed.Implicits._

import java.util.UUID

class GameApi(userService: UserService[IO],
              gameCreator: GameCreator[IO],
              gameService: GameService[IO]) extends TapirApi {

//  private val endpoint = endpoint.in("api")

  private val createGameEndpoint = endpoint.post
    .in(path[UUID]("userId"))
    .in("create-game")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[ProtoGame])
    .serverLogic[IO] { uuid =>
      gameCreator.createGame(uuid.typed[User]).map(x => Right(x)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val joinPlayerEndpoint = endpoint.post
    .in(path[UUID]("userId"))
    .in("join-game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[ProtoGame])
    .serverLogic[IO] { case (userId, gameId) =>
      gameCreator.joinPlayer(gameId.typed[Game], userId.typed[User])
        .map(x => Right(x)).handleError { er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val startGameEndpoint = endpoint.post
    .in(path[UUID]("userId"))
    .in("start-game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId) =>
      gameCreator.startGame(gameId.typed[Game], userId.typed[User])
        .map(x => Right(x)).handleError { er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val pawnMoveEndpoint = endpoint.post
    .in(path[UUID]("userId"))
    .in("move" / "pawn-move")
    .in(query[UUID]("gameId"))
    .in(jsonBody[PawnPosition])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId, pawnPosition) =>
      gameService.makeMove(gameId.typed[Game], userId.typed[User], PawnMove(pawnPosition))
        .map(x => Right(x)).handleError { er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val placeWallEndpoint = endpoint.post
    .in(path[UUID]("userId"))
    .in("move" / "place-wall")
    .in(query[UUID]("gameId"))
    .in(jsonBody[WallPosition])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId, wallPosition) =>
      gameService.makeMove(gameId.typed[Game], userId.typed[User], PlaceWall(wallPosition))
        .map(x => Right(x)).handleError { er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val gameHistoryEndpoint = endpoint.get
    .in(path[UUID]("userId"))
    .in("game")
    .in("history")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[Game]])
    .serverLogic[IO] { case (userId, gameId) =>
      gameService.gameHistory(gameId.typed[Game], userId.typed[User])
        .map(x => Right(x)).handleError { er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val historyEndpoint = endpoint.get
    .in(path[UUID]("userId"))
    .in("history")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[List[GamePreView]])
    .serverLogic[IO] { userId =>
      userService.usersHistory(userId.typed[User]).map(Right(_)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val getGameEndpoint = endpoint.get
    .in(path[UUID]("userId"))
    .in("game")
    .in(query[UUID]("gameId"))
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId) =>
      gameService.findGame(gameId.typed[Game], userId.typed[User]).map(Right(_)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }


  private val moveEndpoint = endpoint.post
    .in(path[UUID]("userId"))
    .in("move")
    .in(query[UUID]("gameId"))
    .in(jsonBody[Move])
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[Game])
    .serverLogic[IO] { case (userId, gameId, move) =>
      gameService.makeMove(gameId.typed[Game], userId.typed[User], move)
        .map(x => Right(x)).handleError { er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  override val api =
    List(
      createGameEndpoint,
      joinPlayerEndpoint,
      startGameEndpoint,
      pawnMoveEndpoint,
      placeWallEndpoint,
      gameHistoryEndpoint,
      historyEndpoint,
      getGameEndpoint,
      moveEndpoint
    )
}
