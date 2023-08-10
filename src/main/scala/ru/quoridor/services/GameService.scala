package ru.quoridor.services

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.dao.GameDao
import ru.quoridor.model.game.geometry.{PawnPosition, WallPosition}
import ru.utils.tagging.Id
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait GameService {
  def findGame(gameId: Id[Game]): Task[Game]

  def makeMove(gameId: Id[Game], userId: Id[User], move: Move): Task[Game]

  def usersHistory(userId: Id[User]): Task[List[GamePreView]]

  def gameHistory(gameId: Id[Game], userId: Id[User]): Task[List[Game]]

  def availablePawnMoves(
      gameId: Id[Game],
      userId: Id[User]
  ): Task[List[PawnPosition]]

  def availableWallMoves(
      gameId: Id[Game]
  ): Task[Set[WallPosition]]
}

object GameService {
  val live: URLayer[GameDao, GameService] =
    ZLayer.fromFunction(new GameServiceImpl(_))

  def findGame(gameId: Id[Game]): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.findGame(gameId))

  def makeMove(
      gameId: Id[Game],
      userId: Id[User],
      move: Move
  ): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.makeMove(gameId, userId, move))

  def usersHistory(userId: Id[User]): RIO[GameService, List[GamePreView]] =
    ZIO.serviceWithZIO[GameService](_.usersHistory(userId))

  def gameHistory(
      gameId: Id[Game],
      userId: Id[User]
  ): RIO[GameService, List[Game]] =
    ZIO.serviceWithZIO[GameService](_.gameHistory(gameId, userId))

  def availablePawnMoves(
      gameId: Id[Game],
      userId: Id[User]
  ): RIO[GameService, List[PawnPosition]] =
    ZIO.serviceWithZIO[GameService](_.availablePawnMoves(gameId, userId))

  def availableWallMoves(
      gameId: Id[Game]
  ): RIO[GameService, Set[WallPosition]] =
    ZIO.serviceWithZIO[GameService](_.availableWallMoves(gameId))
}
