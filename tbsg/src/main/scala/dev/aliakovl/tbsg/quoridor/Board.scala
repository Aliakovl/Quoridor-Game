package dev.aliakovl.tbsg.quoridor

import scala.compiletime.erasedValue

class Board(size: Int):
  private val minCell: Int = 0
  private val midCell: Int = size / 2
  private val maxCell: Int = size - 1
  private val minGroove: Int = minCell
  private val maxGroove: Int = maxCell - 1
  private val totalWalls: Int = 2 * (size + 1)

  private inline def initializeCell(inline side: Side): Cell = inline side match
    case Side.Bottom => Cell(maxCell, midCell)
    case Side.Top    => Cell(minCell, midCell)
    case Side.Left   => Cell(midCell, minCell)
    case Side.Right  => Cell(midCell, maxCell)

  private inline def wallsPerPawn(playersCount: PlayersCount): Int =
    playersCount match
      case PlayersCount.TwoPlayers  => totalWalls / 2
      case PlayersCount.FourPlayers => totalWalls / 4

  private transparent inline def initializePawn[T <: Pawn](
      inline side: Side,
      playersCount: PlayersCount
  ): Pawn = {
    inline erasedValue[T] match
      case _: Pawn.ActivePawn =>
        Pawn.ActivePawn(side, initializeCell(side), wallsPerPawn(playersCount))
      case _: Pawn.WaitingPawn =>
        Pawn.WaitingPawn(side, initializeCell(side), wallsPerPawn(playersCount))
      case _: Pawn.NonActivePawn =>
        Pawn.NonActivePawn(side, initializeCell(side), wallsPerPawn(playersCount))
  }

  def initializePawns(playersCount: PlayersCount): Pawns = {
    playersCount match
      case PlayersCount.TwoPlayers =>
        Pawns(
          activePawn = initializePawn[Pawn.ActivePawn](
            Side.Bottom,
            playersCount
          ),
          waitingPawns = Set(
            initializePawn[Pawn.WaitingPawn](
              Side.Top,
              playersCount
            )
          )
        )
      case PlayersCount.FourPlayers =>
        Pawns(
          activePawn = initializePawn[Pawn.ActivePawn](
            Side.Bottom,
            playersCount
          ),
          waitingPawns = Set(
            initializePawn[Pawn.WaitingPawn](
              Side.Top,
              playersCount
            ),
            initializePawn[Pawn.WaitingPawn](
              Side.Left,
              playersCount
            ),
            initializePawn[Pawn.WaitingPawn](
              Side.Right,
              playersCount
            )
          )
        )
  }
end Board
