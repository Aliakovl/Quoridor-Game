package ru.quoridor.storage

import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.quoridor.game.geometry.Side
import ru.utils.Typed.ID


trait ProtoGameStorage[F[_]] {
  def find(gameId: ID[Game]): F[ProtoGame]

  def insert(userId: ID[User]): F[ProtoGame]

  def update(gameId: ID[Game], userId: ID[User], target: Side): F[ProtoGame]
}