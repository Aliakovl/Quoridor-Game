package model.api

import cats.effect.IO
import model.services.{GameCreator, GameService, UserService}
import model.ExceptionResponse
import model.ProtoGame
import model.User
import model.game.{Game, PawnMove, PlaceWall}
import model.game.geometry.{PawnPosition, WallPosition}
import utils.Typed.Implicits._
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto._
import io.circe.generic.auto._

import java.util.UUID

class GameApi(userService: UserService[IO],
              gameCreator: GameCreator[IO],
              gameService: GameService[IO]) extends TapirApi {

  private val ep = endpoint.in("api")

  private val createGameEndpoint = ep.post
    .in(path[UUID]("userId"))
    .in("create-game")
    .errorOut(jsonBody[ExceptionResponse])
    .out(jsonBody[ProtoGame])
    .serverLogic[IO] { uuid =>
      gameCreator.createGame(uuid.typed[User]).map(x => Right(x)).handleError{ er =>
        Left(ExceptionResponse(er.getMessage))
      }
    }

  private val joinPlayerEndpoint = ep.post
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

  private val startGameEndpoint = ep.post
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

  private val pawnMoveEndpoint = ep.post
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

  private val placeWallEndpoint = ep.post
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

  private val gameHistoryEndpoint = ep.get
    .in(path[UUID]("userId"))
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


  val api =
    List(
      createGameEndpoint,
      joinPlayerEndpoint,
      startGameEndpoint,
      pawnMoveEndpoint,
      placeWallEndpoint,
      gameHistoryEndpoint
    )
}
