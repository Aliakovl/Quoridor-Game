package model.services

import model.game.{Game, Move}

import java.util.UUID


trait GameService[F[_]] {
  def makeMove(gameId: UUID, userId: UUID, move: Move): F[Game]
}
