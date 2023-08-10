package ru.quoridor.model

import ru.quoridor.auth.model.{UserSecret, Username}
import ru.quoridor.model.game.geometry.{PawnPosition, Side}
import ru.utils.tagging.Id

import scala.language.implicitConversions

sealed trait User {
  def id: Id[User]
  def username: Username
}

object User {
  final case class Userdata(
      id: Id[User],
      username: Username
  ) extends User

  final case class UserdataWithSecret(
      id: Id[User],
      username: Username,
      userSecret: UserSecret
  ) extends User

  final case class ProtoPlayer(
      id: Id[User],
      username: Username,
      target: Side
  ) extends User

  final case class Player(
      id: Id[User],
      username: Username,
      pawnPosition: PawnPosition,
      wallsAmount: Int,
      target: Side
  ) extends User

  object Player {
    implicit val ord: Ordering[User.Player] =
      Ordering.by[User.Player, Side](_.target)
  }

  implicit def toUserdata(userdata: UserdataWithSecret): Userdata =
    Userdata(userdata.id, userdata.username)
}
