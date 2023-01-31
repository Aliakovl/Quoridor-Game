package ru.quoridor.services

import ru.quoridor.User
import ru.quoridor.game.{Game, Move}
import ru.utils.Typed.ID


trait GameService[F[_]] {
  def findGame(gameId: ID[Game]): F[Game]

  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): F[Game]

  def gameHistory(gameId: ID[Game], userId: ID[User]): F[List[Game]]
}
