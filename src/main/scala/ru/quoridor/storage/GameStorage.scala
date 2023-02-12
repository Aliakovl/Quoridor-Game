package ru.quoridor.storage

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, Move, State}
import ru.quoridor.storage.sqlStorage.GameStorageImpl
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait GameStorage {
  def find(gameId: ID[Game]): Task[Game]

  def find(gameId: ID[Game], step: Int): Task[Game]

  def lastStep(gameId: ID[Game]): Task[Int]

  def insert(
      gameId: ID[Game],
      step: Int,
      state: State,
      move: Move,
      winner: Option[User]
  ): Task[Unit]

  def create(protoGameId: ID[Game], state: State): Task[Game]

  def hasStarted(gameId: ID[Game]): Task[Boolean]

  def history(id: ID[User]): Task[List[ID[Game]]]

  def findParticipants(gameId: ID[Game]): Task[GamePreView]
}

object GameStorage {
  val live: RLayer[DataBase, GameStorage] =
    ZLayer.fromFunction(new GameStorageImpl(_))
}
