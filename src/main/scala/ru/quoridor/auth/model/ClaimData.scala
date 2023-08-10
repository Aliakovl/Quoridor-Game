package ru.quoridor.auth.model

import ru.quoridor.model.User
import ru.utils.tagging.Id

final case class ClaimData(userId: Id[User], username: Username)
