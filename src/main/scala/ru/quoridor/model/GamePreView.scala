package ru.quoridor.model

import ru.quoridor.model.User.Userdata
import ru.quoridor.model.game.Game
import ru.utils.tagging.Id

final case class GamePreView(
    id: Id[Game],
    players: List[Userdata],
    winner: Option[Userdata]
)
