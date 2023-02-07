package ru.quoridor.model

import ru.utils.tagging.ID

case class User(id: ID[User], login: String)
