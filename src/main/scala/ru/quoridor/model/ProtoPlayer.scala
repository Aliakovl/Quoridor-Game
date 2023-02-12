package ru.quoridor.model

import ru.quoridor.model.game.geometry.Side
import ru.utils.tagging.ID

case class ProtoPlayer(id: ID[User], login: String, target: Side) {
  def toUser: User = User(id, login)
}
