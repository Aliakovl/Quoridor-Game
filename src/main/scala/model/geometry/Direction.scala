package model.geometry


sealed trait Orientation {
  val opposite: Orientation
}
case object Horizontal extends Orientation {
  override val opposite: Orientation = Vertical
}
case object Vertical extends Orientation {
  override val opposite: Orientation = Horizontal
}

case class PawnPosition(row: Int, column: Int)
case class WallPosition(orientation: Orientation, row: Int, column: Int)


sealed trait Direction {
  val crossDirections: (Direction, Direction)

  def toWallPosition(pawnPosition: PawnPosition): WallPosition

  def oneStep(pawnPosition: PawnPosition): PawnPosition
}

object Up extends Direction {
  override val crossDirections: (Direction, Direction) = (ToLeft, ToRight)

  override def toWallPosition(pawnPosition: PawnPosition): WallPosition = pawnPosition match {
    case PawnPosition(row, column) => WallPosition(Horizontal, row, column)
  }

  override def oneStep(pawnPosition: PawnPosition): PawnPosition = {
    pawnPosition.copy(row = pawnPosition.row - 1)
  }
}

object Down extends Direction {
  override val crossDirections: (Direction, Direction) = (ToLeft, ToRight)

  override def toWallPosition(pawnPosition: PawnPosition): WallPosition = pawnPosition match {
    case PawnPosition(row, column) => WallPosition(Horizontal, row + 1, column)
  }

  override def oneStep(pawnPosition: PawnPosition): PawnPosition = {
    pawnPosition.copy(row = pawnPosition.row + 1)
  }
}

object ToLeft extends Direction {
  override val crossDirections: (Direction, Direction) = (Up, Down)

  override def toWallPosition(pawnPosition: PawnPosition): WallPosition = pawnPosition match {
    case PawnPosition(row, column) => WallPosition(Horizontal, column, row)
  }

  override def oneStep(pawnPosition: PawnPosition): PawnPosition = {
    pawnPosition.copy(column = pawnPosition.column - 1)
  }
}

object ToRight extends Direction {
  override val crossDirections: (Direction, Direction) = (Up, Down)

  override def toWallPosition(pawnPosition: PawnPosition): WallPosition = pawnPosition match {
    case PawnPosition(row, column) => WallPosition(Horizontal, column + 1, row)
  }

  override def oneStep(pawnPosition: PawnPosition): PawnPosition = {
    pawnPosition.copy(column = pawnPosition.column + 1)
  }
}
