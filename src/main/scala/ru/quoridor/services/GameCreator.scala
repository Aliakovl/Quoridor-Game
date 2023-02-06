package ru.quoridor.services

import ru.quoridor.model.game.Game
import ru.quoridor.model.ProtoGame
import ru.quoridor.storage.{GameStorage, ProtoGameStorage}
import zio.{RIO, Task, ZIO, ZLayer}

import java.util.UUID

trait GameCreator {
  def createGame(userId: UUID): Task[ProtoGame]

  def joinPlayer(gameId: UUID, playerId: UUID): Task[ProtoGame]

  def startGame(gameId: UUID, userId: UUID): Task[Game]
}

object GameCreator {
  val live
      : ZLayer[ProtoGameStorage with GameStorage, Nothing, GameCreatorImpl] =
    ZLayer.fromFunction(GameCreatorImpl.apply _)

  def createGame(userId: UUID): RIO[GameCreator, ProtoGame] =
    ZIO.serviceWithZIO[GameCreator](_.createGame(userId))

  def joinPlayer(
      gameId: UUID,
      playerId: UUID
  ): RIO[GameCreator, ProtoGame] =
    ZIO.serviceWithZIO[GameCreator](_.joinPlayer(gameId, playerId))

  def startGame(gameId: UUID, userId: UUID): RIO[GameCreator, Game] =
    ZIO.serviceWithZIO[GameCreator](_.startGame(gameId, userId))
}
