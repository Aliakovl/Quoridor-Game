package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.quoridor.model.game.geometry.{PawnPosition, Side}
import ru.utils.tagging.ID

sealed trait User

object User {
  final case class Userdata(
      id: ID[User],
      username: Username
  ) extends User

  final case class ProtoPlayer(
      id: ID[User],
      username: Username,
      target: Side
  ) extends User

  final case class Player(
      id: ID[User],
      username: Username,
      pawnPosition: PawnPosition,
      wallsAmount: Int,
      target: Side
  ) extends User

  object Player {
    implicit val ord: Ordering[Player] = Ordering.by[Player, Side](_.target)
  }

}
