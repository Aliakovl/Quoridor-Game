package model.services

import model.{ProtoGame, User}
import model.game.Game

import java.util.UUID
import scala.concurrent.Future

trait GameCreator {
  def createGame(userId: UUID): Future[ProtoGame]

  def joinPlayer(gameId: UUID, playerId: UUID): Future[ProtoGame]

  def startGame(gameId: UUID): Future[Game]
}
