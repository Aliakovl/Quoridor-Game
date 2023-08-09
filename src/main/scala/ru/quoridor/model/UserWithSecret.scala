package ru.quoridor.model

import ru.quoridor.auth.model.{UserSecret, Username}
import ru.quoridor.model.User.Userdata
import ru.utils.tagging.ID

final case class UserWithSecret(
    id: ID[User],
    username: Username,
    userSecret: UserSecret
) {
  def toUser: Userdata = User.Userdata(id, username)
}
