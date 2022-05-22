package ru.quoridor.game

import ru.quoridor.User
import ru.utils.Typed.ID


case class Game(id: ID[Game], state: State, winner: Option[User])