package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.utils.tagging.ID

sealed trait User

object User {
  final case class Userdata(id: ID[User], username: Username)
}
