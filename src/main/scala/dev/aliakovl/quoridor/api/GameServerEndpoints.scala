package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.services.{GameCreator, GameService, UserService}
import sttp.tapir.ztapir.*
import zio.{URLayer, ZLayer}

class GameServerEndpoints(
    gameService: GameService,
    gameCreator: GameCreator,
    userService: UserService,
    gameEndpoint: GameEndpoints
):
  val createGameServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.createGameEndpoint
      .serverLogic { claimData => _ =>
        gameCreator.createGame(claimData.userId)
      }

  val joinPlayerServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.joinPlayerEndpoint
      .serverLogic { _ => (gameId, userId) =>
        gameCreator.joinPlayer(gameId, userId)
      }

  val startGameServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.startGameEndpoint
      .serverLogic { claimData => gameId =>
        gameCreator.startGame(gameId, claimData.userId)
      }

  val gameHistoryServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.gameHistoryEndpoint
      .serverLogic { claimData => gameId =>
        gameService.gameHistory(gameId, claimData.userId)
      }

  val historyServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.historyEndpoint
      .serverLogic { claimData => _ =>
        gameService.usersHistory(claimData.userId)
      }

  val getGameServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.getGameEndpoint
      .serverLogic { _ => gameId =>
        gameService.findGame(gameId)
      }

  val getUserServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.getUserEndpoint
      .serverLogic { _ => username =>
        userService.getUser(username)
      }

  val pawnMoveServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.pawnMoveEndpoint
      .serverLogic { claimData => (gameId, move) =>
        gameService.makeMove(gameId, claimData.userId, move).unit
      }

  val placeWallServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.placeWallEndpoint
      .serverLogic { claimData => (gameId, move) =>
        gameService.makeMove(gameId, claimData.userId, move).unit
      }

  val possiblePawnMovesServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.possiblePawnMovesEndpoint
      .serverLogic { claimData => gameId =>
        gameService.possiblePawnMoves(gameId, claimData.userId)
      }

  val possibleWallMovesServerEndpoint: ZServerEndpoint[Any, Any] =
    gameEndpoint.possibleWallMovesEndpoint
      .serverLogic { _ => gameId =>
        gameService.possibleWallMoves(gameId)
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
