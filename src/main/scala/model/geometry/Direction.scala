package model.geometry


sealed trait Orientation
object Horizontal extends Orientation
object Vertical extends Orientation

case class GridPosition(row: Int, column: Int)
case class WallPosition(orientation: Orientation, row: Int, column: Int)


sealed trait Direction {
  val crossDirections: (Direction, Direction)

  def wallPosition(gridPosition: GridPosition): WallPosition

  def oneStep(gridPosition: GridPosition): GridPosition
}

object Up extends Direction {
  override val crossDirections: (Direction, Direction) = (ToLeft, ToRight)

  override def wallPosition(gridPosition: GridPosition): WallPosition = {
    val (row, column): (Int, Int) = gridPosition
    WallPosition(Horizontal, row, column)
  }

  override def oneStep(gridPosition: GridPosition): GridPosition = {
    gridPosition.copy(row = gridPosition.row - 1)
  }
}

object Down extends Direction {
  override val crossDirections: (Direction, Direction) = (ToLeft, ToRight)

  override def wallPosition(gridPosition: GridPosition): WallPosition = {
    val (row, column): (Int, Int) = gridPosition
    WallPosition(Horizontal, row + 1, column)
  }

  override def oneStep(gridPosition: GridPosition): GridPosition = {
    gridPosition.copy(row = gridPosition.row + 1)
  }
}

object ToLeft extends Direction {
  override val crossDirections: (Direction, Direction) = (Up, Down)

  override def wallPosition(gridPosition: GridPosition): WallPosition = {
    val (row, column): (Int, Int) = gridPosition
    WallPosition(Vertical, column, row)
  }

  override def oneStep(gridPosition: GridPosition): GridPosition = {
    gridPosition.copy(column = gridPosition.column - 1)
  }
}

object ToRight extends Direction {
  override val crossDirections: (Direction, Direction) = (Up, Down)

  override def wallPosition(gridPosition: GridPosition): WallPosition = {
    val (row, column): (Int, Int) = gridPosition
    WallPosition(Vertical, column + 1, row)
  }

  override def oneStep(gridPosition: GridPosition): GridPosition = {
    gridPosition.copy(column = gridPosition.column + 1)
  }
}
