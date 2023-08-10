package ru.quoridor.model

import ru.quoridor.auth.model.{UserSecret, Username}
import ru.quoridor.model.game.geometry.{PawnPosition, Side}
import ru.utils.tagging.ID

sealed trait User {
  def id: ID[User]
  def username: Username
}

object User {
  final case class Userdata(
      id: ID[User],
      username: Username
  ) extends User

  final case class UserdataWithSecret(
      id: ID[User],
      username: Username,
      userSecret: UserSecret
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
    implicit val ord: Ordering[User.Player] = Ordering.by[User.Player, Side](_.target)
  }
}
