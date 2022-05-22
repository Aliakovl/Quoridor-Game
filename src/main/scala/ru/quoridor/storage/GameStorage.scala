package ru.quoridor.storage

import ru.quoridor.{GamePreView, User}
import ru.quoridor.game.{Game, State}
import ru.utils.Typed.ID


trait GameStorage[F[_]] {
  def find(id: ID[Game]): F[Game]

  def insert(previousGameId: ID[Game], state: State, winner: Option[User]): F[Game]

  def create(protoGameId: ID[Game], state: State): F[Game]

  def exists(gameId: ID[Game]): F[Boolean]

  def gameHistory(gameId: ID[Game]): F[List[ID[Game]]]

  def findParticipants(gameId: ID[Game]): F[GamePreView]
}
