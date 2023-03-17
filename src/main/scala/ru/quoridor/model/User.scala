package ru.quoridor.model

import ru.quoridor.auth.model.Username
import ru.utils.tagging.ID

case class User(id: ID[User], username: Username)
