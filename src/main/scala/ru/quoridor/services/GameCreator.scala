package ru.quoridor.services

import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.utils.Typed.ID
import zio.Task

trait GameCreator {
  def createGame(userId: ID[User]): Task[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): Task[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): Task[Game]
}
