package ru.quoridor.services

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.dao.GameDao
import ru.utils.tagging.ID
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait GameService {
  def findGame(gameId: ID[Game]): Task[Game]

  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): Task[Game]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]

  def gameHistory(gameId: ID[Game], userId: ID[User]): Task[List[Game]]
}

object GameService {
  val live: URLayer[GameDao, GameService] =
    ZLayer.fromFunction(new GameServiceImpl(_))

  def findGame(gameId: ID[Game]): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.findGame(gameId))

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
}
