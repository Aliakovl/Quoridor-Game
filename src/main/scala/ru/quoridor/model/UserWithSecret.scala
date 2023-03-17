package ru.quoridor.model

import ru.quoridor.auth.model.{UserSecret, Username}
import ru.utils.tagging.ID

case class UserWithSecret(
    id: ID[User],
    username: Username,
    userSecret: UserSecret
) {
  def toUser: User = User(id, username)
}
