package ru.quoridor.services

import ru.quoridor.engine.model.{GamePreView, User}
import ru.quoridor.engine.model.game.{Game, Move}
import ru.quoridor.dao.GameDao
import ru.quoridor.engine.model.game.geometry.{PawnPosition, WallPosition}
import ru.quoridor.pubsub.GamePubSub
import ru.utils.tagging.ID
import zio.stream.ZStream
import zio.{RIO, Task, ZIO, ZLayer}

trait GameService {
  def findGame(gameId: ID[Game]): Task[Game]

  def subscribeOnGame(
      gameId: ID[Game]
  ): Task[ZStream[Any, Throwable, Game]]

  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): Task[Game]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]

  def gameHistory(gameId: ID[Game], userId: ID[User]): Task[List[Game]]

  def availablePawnMoves(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[PawnPosition]]

  def availableWallMoves(
      gameId: ID[Game]
  ): Task[Set[WallPosition]]
}

object GameService {
  val live: ZLayer[
    GameDao with GamePubSub,
    Nothing,
    GameService
  ] =
    ZLayer.fromFunction(new GameServiceImpl(_, _))

  def findGame(gameId: ID[Game]): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.findGame(gameId))

  def subscribeOnGame(
      gameId: ID[Game]
  ): RIO[GameService, ZStream[Any, Throwable, Game]] =
    ZIO.serviceWithZIO[GameService](_.subscribeOnGame(gameId))

  def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.makeMove(gameId, userId, move))

  def usersHistory(userId: ID[User]): RIO[GameService, List[GamePreView]] =
    ZIO.serviceWithZIO[GameService](_.usersHistory(userId))

  def gameHistory(
      gameId: ID[Game],
      userId: ID[User]
  ): RIO[GameService, List[Game]] =
    ZIO.serviceWithZIO[GameService](_.gameHistory(gameId, userId))

  def availablePawnMoves(
      gameId: ID[Game],
      userId: ID[User]
  ): RIO[GameService, List[PawnPosition]] =
    ZIO.serviceWithZIO[GameService](_.availablePawnMoves(gameId, userId))

  def availableWallMoves(
      gameId: ID[Game]
  ): RIO[GameService, Set[WallPosition]] =
    ZIO.serviceWithZIO[GameService](_.availableWallMoves(gameId))
}
