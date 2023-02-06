package ru.quoridor.model

import ru.utils.Typed.ID

case class User(id: ID[User], login: String)
