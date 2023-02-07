package ru.quoridor.services

import ru.quoridor.model.User
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.storage.GameStorage
import ru.utils.Tagged.ID
import zio.{RIO, Task, ZIO, ZLayer}

trait GameService {
  def findGame(gameId: ID[Game]): Task[Game]

  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): Task[Game]

  def gameHistory(gameId: ID[Game], userId: ID[User]): Task[List[Game]]
}

object GameService {
  val live: ZLayer[GameStorage, Nothing, GameServiceImpl] =
    ZLayer.fromFunction(GameServiceImpl.apply _)

  def findGame(gameId: ID[Game]): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.findGame(gameId))

  def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.makeMove(gameId, userId, move))

  def gameHistory(
      gameId: ID[Game],
      userId: ID[User]
  ): RIO[GameService, List[Game]] =
    ZIO.serviceWithZIO[GameService](_.gameHistory(gameId, userId))
}
