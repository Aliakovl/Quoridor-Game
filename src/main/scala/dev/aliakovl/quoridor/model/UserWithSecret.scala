package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.auth.model.{UserSecret, Username}
import dev.aliakovl.utils.tagging.ID

case class UserWithSecret(
    id: ID[User],
    username: Username,
    userSecret: UserSecret
) {
  def toUser: User = User(id, username)
}
