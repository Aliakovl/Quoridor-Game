package ru.quoridor.services

import ru.quoridor.model.game.Game
import ru.quoridor.model.{ProtoGame, User}
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import ru.utils.tagging.ID
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait GameCreator {
  def createGame(userId: ID[User]): Task[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): Task[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): Task[Game]
}

object GameCreator {
  val live: URLayer[
    UserDao with ProtoGameDao with GameDao,
    GameCreator
  ] =
    ZLayer.fromFunction(new GameCreatorImpl(_, _, _))

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
