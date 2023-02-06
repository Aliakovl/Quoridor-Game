package ru.quoridor.storage

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, State}
import ru.quoridor.storage.sqlStorage.GameStorageImpl
import zio.{RLayer, Task, ZLayer}

import java.util.UUID

trait GameStorage {
  def find(gameId: UUID): Task[Game]

  def insert(
      previousGameId: UUID,
      state: State,
      winner: Option[User]
  ): Task[Game]

  def create(protoGameId: UUID, state: State): Task[Game]

  def exists(gameId: UUID): Task[Boolean]

  def gameHistory(gameId: UUID): Task[List[UUID]]

  def findParticipants(gameId: UUID): Task[GamePreView]
}

object GameStorage {
  val live: RLayer[DataBase, GameStorage] =
    ZLayer.fromFunction(GameStorageImpl.apply _)
}
