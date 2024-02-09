package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.quoridor
import dev.aliakovl.tbsg.quoridor.Pawn.*
import dev.aliakovl.tbsg.quoridor.PlayersCount.*

case class Pawns(activePawn: ActivePawn, waitingPawns: Set[WaitingPawn])

object Pawns:
  def initialize(
      playersCount: PlayersCount,
      board: Board
  ): Pawns = {
    inline transparent def initializePawn[T <: Pawn](
        inline side: Side
    ): Pawn = {
      Pawn[T](
        side,
        board.initializeCell(side),
        board.wallsPerPawn(playersCount)
      )
    }

    playersCount match
      case TwoPlayers =>
        Pawns(
          activePawn = initializePawn[ActivePawn](Side.Bottom),
          waitingPawns = Set(
            initializePawn[WaitingPawn](Side.Top)
          )
        )
      case FourPlayers =>
        Pawns(
          activePawn = initializePawn[ActivePawn](Side.Bottom),
          waitingPawns = Set(
            initializePawn[WaitingPawn](Side.Top),
            initializePawn[WaitingPawn](Side.Left),
            initializePawn[WaitingPawn](Side.Right)
          )
        )
  }
end Pawns
