package ru.quoridor.services

import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.quoridor.storage.{GameStorage, ProtoGameStorage}
import ru.utils.Typed.ID
import zio.{RIO, Task, ZIO, ZLayer}

trait GameCreator {
  def createGame(userId: ID[User]): Task[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): Task[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): Task[Game]
}

object GameCreator {
  val live
      : ZLayer[ProtoGameStorage with GameStorage, Nothing, GameCreatorImpl] =
    ZLayer.fromFunction(GameCreatorImpl.apply _)

  def createGame(userId: ID[User]): RIO[GameCreator, ProtoGame] =
    ZIO.serviceWithZIO[GameCreator](_.createGame(userId))

  def joinPlayer(
      gameId: ID[Game],
      playerId: ID[User]
  ): RIO[GameCreator, ProtoGame] =
    ZIO.serviceWithZIO[GameCreator](_.joinPlayer(gameId, playerId))

  def startGame(gameId: ID[Game], userId: ID[User]): RIO[GameCreator, Game] =
    ZIO.serviceWithZIO[GameCreator](_.startGame(gameId, userId))
}
