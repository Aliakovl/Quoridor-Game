package dev.aliakovl.tbsg.quoridor

import cats.syntax.all.*
import cats.data.ValidatedNec
import dev.aliakovl.tbsg.{Rules, quoridor}
import dev.aliakovl.tbsg.quoridor.GameState.*
import dev.aliakovl.tbsg.quoridor.GameEvent.*
import dev.aliakovl.tbsg.quoridor.GameError.*

class Quoridor(private val size: Int)
    extends Rules[
      [A] =>> ValidatedNec[GameError, A],
      Set,
      PlayersCount,
      GameState,
      GameEvent
    ]:
  private val board = Board(size)

  override def initialize(
      playersCount: PlayersCount
  ): ValidatedNec[GameError, GameState] = ActiveGame(
    pawns = Pawns.initialize(playersCount, board),
    walls = Set.empty,
    winnersTable = Seq.empty
  ).valid

  override def handleEvent(
      event: GameEvent,
      state: GameState
  ): ValidatedNec[GameError, GameState] = state match {
    case as @ ActiveGame(_, _, _) => board.move(event, as)
    case EndedGame(_, _)          => IllegalMoveInEndedGameError.invalidNec
  }

  override def permittedActions(state: GameState): Set[GameEvent] = ???

end Quoridor
