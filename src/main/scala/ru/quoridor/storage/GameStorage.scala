package ru.quoridor.storage

import cats.effect.Resource
import doobie.Transactor
import ru.quoridor.{GamePreView, User}
import ru.quoridor.game.{Game, State}
import ru.quoridor.storage.sqlStorage.GameStorageImpl
import ru.utils.Typed.ID
import zio.{Task, ZLayer}

trait GameStorage {
  def find(id: ID[Game]): Task[Game]

  def insert(
      previousGameId: ID[Game],
      state: State,
      winner: Option[User]
  ): Task[Game]

  def create(protoGameId: ID[Game], state: State): Task[Game]

  def exists(gameId: ID[Game]): Task[Boolean]

  def gameHistory(gameId: ID[Game]): Task[List[ID[Game]]]

  def findParticipants(gameId: ID[Game]): Task[GamePreView]
}

object GameStorage {
  val live: ZLayer[Resource[Task, Transactor[Task]], Nothing, GameStorage] =
    ZLayer.fromFunction(GameStorageImpl.apply _)
}
