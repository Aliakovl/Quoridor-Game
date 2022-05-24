package ru.quoridor.game

import ru.quoridor.{GamePreView, User}
import ru.utils.Typed.ID


case class Game(id: ID[Game], state: State, winner: Option[User]) {
  def toGamePreView: GamePreView = GamePreView(id, state.players.toList.map(_.toUser), winner)
}