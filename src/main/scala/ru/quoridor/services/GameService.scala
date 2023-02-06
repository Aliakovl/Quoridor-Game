package ru.quoridor.services

import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.storage.GameStorage
import zio.{RIO, Task, ZIO, ZLayer}

import java.util.UUID

trait GameService {
  def findGame(gameId: UUID): Task[Game]

  def makeMove(gameId: UUID, userId: UUID, move: Move): Task[Game]

  def gameHistory(gameId: UUID, userId: UUID): Task[List[Game]]
}

object GameService {
  val live: ZLayer[GameStorage, Nothing, GameServiceImpl] =
    ZLayer.fromFunction(GameServiceImpl.apply _)

  def findGame(gameId: UUID): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.findGame(gameId))

  def makeMove(
      gameId: UUID,
      userId: UUID,
      move: Move
  ): RIO[GameService, Game] =
    ZIO.serviceWithZIO[GameService](_.makeMove(gameId, userId, move))

  def gameHistory(
      gameId: UUID,
      userId: UUID
  ): RIO[GameService, List[Game]] =
    ZIO.serviceWithZIO[GameService](_.gameHistory(gameId, userId))
}
