package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.Rules
import dev.aliakovl.tbsg.quoridor.GameState.ActiveGame

class Quoridor(private val size: Int)
    extends Rules[
      [A] =>> Either[Error, A],
      Set,
      PlayersCount,
      GameState,
      GameEvent
    ]:
  private val board = Board(size)

  override def initialize(
      playersCount: PlayersCount
  ): Either[Error, GameState] = Right(
    ActiveGame(
      pawns = Pawns.initialize(playersCount, board),
      walls = Set.empty
    )
  )

  override def handleEvent(
      event: GameEvent,
      state: GameState
  ): Either[Error, GameState] = ???

  override def permittedActions(state: GameState): Set[GameEvent] = ???

end Quoridor
