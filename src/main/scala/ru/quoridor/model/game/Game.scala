package ru.quoridor.model.game

import ru.quoridor.model
import ru.quoridor.model.{GamePreView, User}
import ru.utils.tagging.ID

case class Game(id: ID[Game], state: State, winner: Option[User]) {
  def toGamePreView: GamePreView =
    model.GamePreView(id, state.players.toList.map(_.toUser), winner)
}
