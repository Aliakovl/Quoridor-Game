package dev.aliakovl.tbsg.quoridor

class Board(size: Int):
  private val minCell: Int = 0
  private val midCell: Int = size / 2
  private val maxCell: Int = size - 1
  private val minGroove: Int = minCell
  private val maxGroove: Int = maxCell - 1
  private val totalWalls: Int = 2 * (size + 1)

  inline def wallsPerPawn(inline playersCount: PlayersCount): Int =
    totalWalls / playersCount.asInt

  inline def initializeCell(inline side: Side): Cell = inline side match
    case Side.Bottom => Cell(maxCell, midCell)
    case Side.Top    => Cell(minCell, midCell)
    case Side.Left   => Cell(midCell, minCell)
    case Side.Right  => Cell(midCell, maxCell)

end Board
