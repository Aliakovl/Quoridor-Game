package ru.quoridor.model.game

import ru.quoridor.model
import ru.quoridor.model.{GamePreView, User}

import java.util.UUID

case class Game(gameId: UUID, state: State, winner: Option[User]) {
  def toGamePreView: GamePreView =
    model.GamePreView(gameId, state.players.toList.map(_.toUser), winner)
}
