package ru.quoridor.model

import ru.utils.Typed.ID

case class User(userId: ID[User], login: String)
