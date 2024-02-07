package dev.aliakovl.tbsg.quoridor

class Board(size: Int):
  val minCell: Int = 0
  val midCell: Int = size / 2
  val maxCell: Int = size - 1
  val minGroove: Int = minCell
  val maxGroove: Int = maxCell - 1
  val totalWalls: Int = 2 * (size + 1)

  inline private def initialCell(side: Side): Cell = inline side match
    case Side.Bottom => Cell(maxCell, midCell)
    case Side.Top    => Cell(minCell, midCell)
    case Side.Left   => Cell(midCell, minCell)
    case Side.Right  => Cell(midCell, maxCell)

  inline private def wallsPerPawn(playersCount: PlayersCount): Int =
    totalWalls / playersCount.toInt

  inline private def initialPawn[T <: Pawn](
      side: Side,
      walls: Int
  )(using Conversion[(Side, Cell, Int), T]): T =
    summon[Conversion[(Side, Cell, Int), T]](
      side,
      initialCell(side),
      walls
    )

  def initialPawns(playersCount: PlayersCount): Pawns = {
    val initialWallCount = wallsPerPawn(playersCount)
    playersCount match
      case PlayersCount.TwoPlayers =>
        Pawns(
          activePawn =
            initialPawn[Pawn.ActivePawn](Side.Bottom, initialWallCount),
          waitingPawns = Set(
            initialPawn[Pawn.WaitingPawn](Side.Top, initialWallCount)
          )
        )
      case PlayersCount.FourPlayers =>
        Pawns(
          activePawn =
            initialPawn[Pawn.ActivePawn](Side.Bottom, initialWallCount),
          waitingPawns = Set(
            initialPawn[Pawn.WaitingPawn](Side.Top, initialWallCount),
            initialPawn[Pawn.WaitingPawn](Side.Left, initialWallCount),
            initialPawn[Pawn.WaitingPawn](Side.Right, initialWallCount)
          )
        )
  }
