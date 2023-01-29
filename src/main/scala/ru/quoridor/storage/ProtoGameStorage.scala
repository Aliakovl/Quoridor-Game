package ru.quoridor.storage

import ru.quoridor.{ProtoGame, User}
import ru.quoridor.game.Game
import ru.quoridor.game.geometry.Side
import ru.utils.Typed.ID
import zio.Task

trait ProtoGameStorage {
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(userId: ID[User]): Task[ProtoGame]

  def update(gameId: ID[Game], userId: ID[User], target: Side): Task[ProtoGame]
}
