package model.services

import model.User
import model.game.{Game, Move}
import utils.Typed.ID


trait GameService[F[_]] {
  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): F[Game]

  def gameHistory(gameId: ID[Game], userId: ID[User]): F[List[Game]]
}
