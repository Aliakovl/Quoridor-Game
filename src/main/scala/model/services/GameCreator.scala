package model.services

import model.{ProtoGame, User}
import model.game.Game

import java.util.UUID


trait GameCreator[F[_]] {
  def createGame(userId: UUID): F[ProtoGame]

  def joinPlayer(gameId: UUID, playerId: UUID): F[ProtoGame]

  def startGame(gameId: UUID): F[Game]
}
