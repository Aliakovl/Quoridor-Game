package dev.aliakovl.tbsg.quoridor

import scala.compiletime.erasedValue

class Board(size: Int):
  val minCell: Int = 0
  val midCell: Int = size / 2
  val maxCell: Int = size - 1
  val minGroove: Int = minCell
  val maxGroove: Int = maxCell - 1
  val totalWalls: Int = 2 * (size + 1)

  inline private def initializeCell(inline side: Side): Cell = inline side match
    case Side.Bottom => Cell(maxCell, midCell)
    case Side.Top    => Cell(minCell, midCell)
    case Side.Left   => Cell(midCell, minCell)
    case Side.Right  => Cell(midCell, maxCell)

  inline private def wallsPerPawn(inline playersCount: PlayersCount): Int =
    inline playersCount match
      case PlayersCount.TwoPlayers  => totalWalls / 2
      case PlayersCount.FourPlayers => totalWalls / 4

  private transparent inline def initialPawn[T <: Pawn](
      inline side: Side,
      inline walls: Int
  ): Pawn = {
    inline erasedValue[T] match
      case _: Pawn.ActivePawn =>
        Pawn.ActivePawn(side, initializeCell(side), walls)
      case _: Pawn.WaitingPawn =>
        Pawn.WaitingPawn(side, initializeCell(side), walls)
      case _: Pawn.NonActivePawn =>
        Pawn.NonActivePawn(side, initializeCell(side), walls)
  }

  def initialPawns(playersCount: PlayersCount): Pawns = {
    playersCount match
      case PlayersCount.TwoPlayers =>
        Pawns(
          activePawn = initialPawn[Pawn.ActivePawn](
            Side.Bottom,
            wallsPerPawn(PlayersCount.TwoPlayers)
          ),
          waitingPawns = Set(
            initialPawn[Pawn.WaitingPawn](
              Side.Top,
              wallsPerPawn(PlayersCount.TwoPlayers)
            )
          )
        )
      case PlayersCount.FourPlayers =>
        Pawns(
          activePawn = initialPawn[Pawn.ActivePawn](
            Side.Bottom,
            wallsPerPawn(PlayersCount.FourPlayers)
          ),
          waitingPawns = Set(
            initialPawn[Pawn.WaitingPawn](
              Side.Top,
              wallsPerPawn(PlayersCount.FourPlayers)
            ),
            initialPawn[Pawn.WaitingPawn](
              Side.Left,
              wallsPerPawn(PlayersCount.FourPlayers)
            ),
            initialPawn[Pawn.WaitingPawn](
              Side.Right,
              wallsPerPawn(PlayersCount.FourPlayers)
            )
          )
        )
  }
