package ru.quoridor.services

import ru.quoridor.model.game.Game
import ru.quoridor.model.{ProtoGame, User}
import ru.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import ru.utils.tagging.Id
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait GameCreator {
  def createGame(userId: Id[User]): Task[ProtoGame]

  def joinPlayer(gameId: Id[Game], playerId: Id[User]): Task[ProtoGame]

  def startGame(gameId: Id[Game], userId: Id[User]): Task[Game]
}

object GameCreator {
  val live: URLayer[
    UserDao with ProtoGameDao with GameDao,
    GameCreator
  ] =
    ZLayer.fromFunction(new GameCreatorImpl(_, _, _))

  def createGame(userId: Id[User]): RIO[GameCreator, ProtoGame] =
    ZIO.serviceWithZIO[GameCreator](_.createGame(userId))

  def joinPlayer(
      gameId: Id[Game],
      playerId: Id[User]
  ): RIO[GameCreator, ProtoGame] =
    ZIO.serviceWithZIO[GameCreator](_.joinPlayer(gameId, playerId))

  def startGame(gameId: Id[Game], userId: Id[User]): RIO[GameCreator, Game] =
    ZIO.serviceWithZIO[GameCreator](_.startGame(gameId, userId))
}
