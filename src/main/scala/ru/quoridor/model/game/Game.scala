package ru.quoridor.model.game

import ru.quoridor.model.User.Userdata
import ru.utils.tagging.Id

final case class Game(
    id: Id[Game],
    step: Int,
    state: State,
    winner: Option[Userdata]
)
