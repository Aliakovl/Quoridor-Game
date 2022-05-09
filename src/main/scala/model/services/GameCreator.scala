package model.services

import model.{ProtoGame, User}
import model.game.Game
import utils.Typed.ID


trait GameCreator[F[_]] {
  def createGame(userId: ID[User]): F[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): F[ProtoGame]

  def startGame(gameId: ID[Game]): F[Game]
}
