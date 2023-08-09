package ru.quoridor.model

import ru.quoridor.model.User.Userdata
import ru.quoridor.model.game.Game
import ru.utils.tagging.ID

final case class GamePreView(
    id: ID[Game],
    players: List[Userdata],
    winner: Option[Userdata]
)
