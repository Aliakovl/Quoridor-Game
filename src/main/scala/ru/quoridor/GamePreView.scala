package ru.quoridor

import ru.quoridor.game.Game
import ru.utils.Typed.ID

case class GamePreView(id: ID[Game], players: List[User], winner: Option[User])
