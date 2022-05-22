package ru.quoridor.services

import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.utils.Typed.ID


trait GameCreator[F[_]] {
  def createGame(userId: ID[User]): F[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): F[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): F[Game]
}
