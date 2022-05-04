package model.services

import model.game.{Game, Move}

import java.util.UUID
import scala.concurrent.Future

trait GameService {
  def makeMove(gameId: UUID, userId: UUID, move: Move): Future[Game]
}
