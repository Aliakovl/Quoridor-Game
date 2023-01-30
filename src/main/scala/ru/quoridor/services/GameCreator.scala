package ru.quoridor.services

import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.quoridor.storage.{GameStorage, ProtoGameStorage}
import ru.utils.Typed.ID
import zio.{Task, ZLayer}

trait GameCreator {
  def createGame(userId: ID[User]): Task[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): Task[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): Task[Game]
}

object GameCreator {
  val live
      : ZLayer[ProtoGameStorage with GameStorage, Nothing, GameCreatorImpl] =
    ZLayer.fromFunction(GameCreatorImpl.apply _)
}
