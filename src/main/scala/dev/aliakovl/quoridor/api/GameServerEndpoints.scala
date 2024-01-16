package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.api.ErrorMapping.defaultErrorsMapping
import dev.aliakovl.quoridor.services.{GameCreator, GameService, UserService}
import sttp.tapir.ztapir.*
import zio.{URLayer, ZLayer}

import scala.util.chaining.*

class GameServerEndpoints(
    gameService: GameService,
    gameCreator: GameCreator,
    userService: UserService,
    gameEndpoint: GameEndpoints
):
  private val createGameServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.createGameEndpoint
      .serverLogic { claimData => _ =>
        gameCreator
          .createGame(claimData.userId)
          .pipe(defaultErrorsMapping)
      }

  private val joinPlayerServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.joinPlayerEndpoint
      .serverLogic { _ => (gameId, userId) =>
        gameCreator
          .joinPlayer(gameId, userId)
          .pipe(defaultErrorsMapping)
      }

  private val startGameServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.startGameEndpoint
      .serverLogic { claimData => gameId =>
        gameCreator
          .startGame(gameId, claimData.userId)
          .pipe(defaultErrorsMapping)
      }

  private val gameHistoryServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.gameHistoryEndpoint
      .serverLogic { claimData => gameId =>
        gameService
          .gameHistory(gameId, claimData.userId)
          .pipe(defaultErrorsMapping)
      }

  private val historyServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.historyEndpoint
      .serverLogic { claimData => _ =>
        gameService
          .usersHistory(claimData.userId)
          .pipe(defaultErrorsMapping)
      }

  private val getGameServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.getGameEndpoint
      .serverLogic { _ => gameId =>
        gameService
          .findGame(gameId)
          .pipe(defaultErrorsMapping)
      }

  private val getUserServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.getUserEndpoint
      .serverLogic { _ => username =>
        userService
          .getUser(username)
          .pipe(defaultErrorsMapping)
      }

  private val pawnMoveServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.pawnMoveEndpoint
      .serverLogic { claimData => (gameId, move) =>
        gameService
          .makeMove(gameId, claimData.userId, move)
          .unit
          .pipe(defaultErrorsMapping)
      }

  private val placeWallServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.placeWallEndpoint
      .serverLogic { claimData => (gameId, move) =>
        gameService
          .makeMove(gameId, claimData.userId, move)
          .unit
          .pipe(defaultErrorsMapping)
      }

  private val possiblePawnMovesServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.possiblePawnMovesEndpoint
      .serverLogic { claimData => gameId =>
        gameService
          .possiblePawnMoves(gameId, claimData.userId)
          .pipe(defaultErrorsMapping)
      }

  private val possibleWallMovesServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.possibleWallMovesEndpoint
      .serverLogic { _ => gameId =>
        gameService
          .possibleWallMoves(gameId)
          .pipe(defaultErrorsMapping)
      }

  val endpoints: List[ZServerEndpoint[Any, Any]] = List(
    createGameServerEndpoint,
    joinPlayerServerEndpoint,
    startGameServerEndpoint,
    gameHistoryServerEndpoint,
    historyServerEndpoint,
    getGameServerEndpoint,
    getUserServerEndpoint,
    pawnMoveServerEndpoint,
    placeWallServerEndpoint,
    possiblePawnMovesServerEndpoint,
    possibleWallMovesServerEndpoint
  )

object GameServerEndpoints:
  val live: URLayer[
    GameService & GameCreator & UserService & GameEndpoints,
    GameServerEndpoints
  ] = ZLayer.fromFunction(new GameServerEndpoints(_, _, _, _))
