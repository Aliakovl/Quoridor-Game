package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.Actions
import dev.aliakovl.tbsg.quoridor.GameEvent.{PawnMove, PlaceWall}
import dev.aliakovl.tbsg.quoridor.GameState.ActiveGame

final class QuoridorActions private[quoridor] (board: Board)
    extends Actions[Set, GameState, GameEvent]:
  override def actions(state: GameState): Set[GameEvent] = state match
    case activeGame @ ActiveGame(pawns, walls, _) =>
      activeGame
        .accessibleSteps(board)
        .map(PawnMove(pawns.activePawn.edge, _)) ++ {
        if (activeGame.pawns.activePawn.walls > 0) {
          board
            .accessibleGrooves(pawns, walls)
            .map(PlaceWall(pawns.activePawn.edge, _))
        } else {
          Set.empty
        }
      }
    case _ => Set.empty
end QuoridorActions
