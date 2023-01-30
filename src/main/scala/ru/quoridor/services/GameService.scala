package ru.quoridor.services

import ru.quoridor.User
import ru.quoridor.game.{Game, Move}
import ru.quoridor.storage.GameStorage
import ru.utils.Typed.ID
import zio.{Task, ZLayer}

trait GameService {
  def findGame(gameId: ID[Game]): Task[Game]

  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): Task[Game]

  def gameHistory(gameId: ID[Game], userId: ID[User]): Task[List[Game]]
}

object GameService {
  val live: ZLayer[GameStorage, Nothing, GameServiceImpl] =
    ZLayer.fromFunction(GameServiceImpl.apply _)
}
