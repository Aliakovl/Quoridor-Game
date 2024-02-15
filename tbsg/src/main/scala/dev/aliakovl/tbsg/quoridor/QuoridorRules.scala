package dev.aliakovl.tbsg.quoridor

import cats.syntax.all.*
import cats.data.ValidatedNec
import dev.aliakovl.tbsg.{Rules, quoridor}
import dev.aliakovl.tbsg.quoridor.GameState.*
import dev.aliakovl.tbsg.quoridor.GameError.*

final class QuoridorRules private[quoridor] (board: Board)
    extends Rules[
      [A] =>> ValidatedNec[GameError, A],
      PlayersCount,
      GameEvent,
      GameState
    ]:
  override def initialize(
      playersCount: PlayersCount
  ): ValidatedNec[GameError, GameState] = ActiveGame(
    pawns = Pawns.initialize(playersCount, board),
    walls = Set.empty,
    winnersTable = Seq.empty
  ).valid

  override def handle(
      event: GameEvent,
      state: GameState
  ): ValidatedNec[GameError, GameState] = state match {
    case activeGame @ ActiveGame(_, _, _) => board.move(event, activeGame)
    case _ => IllegalMoveInEndedGameError.invalidNec
  }
end QuoridorRules
