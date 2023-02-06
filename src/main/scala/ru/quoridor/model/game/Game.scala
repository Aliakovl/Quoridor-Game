package ru.quoridor.model.game

import ru.quoridor.model
import ru.quoridor.model.{GamePreView, User}
import ru.utils.Typed.ID

case class Game(gameId: ID[Game], state: State, winner: Option[User]) {
  def toGamePreView: GamePreView =
    model.GamePreView(gameId, state.players.toList.map(_.toUser), winner)
}
