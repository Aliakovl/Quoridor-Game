package model.geometry

import scala.collection.mutable


object Board {
  val width: Int = 9
  val halfWidth: Int = width / 2
  val initPositions: Seq[PawnPosition] = Side.allSides.map(initPosition)

  val borders: Seq[WallPosition] = for {
    index <- 0 until width
    if index % 2 == 0
    top = WallPosition(Horizontal, 0, index)
    bottom = WallPosition(Horizontal, width, index)
    left = WallPosition(Vertical, 0, index)
    right = WallPosition(Vertical, width, index)
    elem <- Seq(top, bottom, left, right)
  } yield elem

  def isOnEdge(position: PawnPosition, side: Side): Boolean = {
    side match {
      case Side.Top => position.row == 0
      case Side.Bottom => position.row == width - 1
      case Side.Left => position.column == 0
      case Side.Right => position.column == width - 1
    }
  }

  def initPosition(side: Side): PawnPosition = {
    side match {
      case Side.Top => PawnPosition(0, halfWidth)
      case Side.Bottom => PawnPosition(width, halfWidth)
      case Side.Left => PawnPosition(halfWidth, 0)
      case Side.Right => PawnPosition(halfWidth, width)
    }
  }

  def isPawnOnBoard(pawnPosition: PawnPosition): Boolean = {
    pawnPosition match {
      case PawnPosition(row, column) =>
        0 <= row && row < width && 0 <= column && column < width
    }
  }

  def adjacentPosition(pawnPosition: PawnPosition,
                       walls: Set[WallPosition],
                       direction: Direction): Option[PawnPosition] = {
    val wp1 = direction.toWallPosition(pawnPosition)
    val wp2 = wp1.copy(column = wp1.column - 1)
    val wayIsBlocked = (walls contains wp1) || (walls contains wp2)
    val newPosition = direction.oneStep(pawnPosition)
    if (!wayIsBlocked && isPawnOnBoard(newPosition)) {
      Some(newPosition)
    } else {
      None
    }
  }

  def adjacentPositions(pawnPosition: PawnPosition,
                        walls: Set[WallPosition]): Seq[PawnPosition] = {
    Seq(
      adjacentPosition(pawnPosition, walls, Up),
      adjacentPosition(pawnPosition, walls, Down),
      adjacentPosition(pawnPosition, walls, ToLeft),
      adjacentPosition(pawnPosition, walls, ToRight)
    ).flatten
  }

  def isWallOnBoard(wallPosition: WallPosition): Boolean = {
    wallPosition match {
      case WallPosition(_, row, column) =>
        0 <= row && row < (width - 1) && 0 <= column && column < (width - 1)
    }
  }

  def doWallsIntersect(wall: WallPosition, otherWall: WallPosition): Boolean = {
    wall match {
      case WallPosition(orientation, row, column) =>
        otherWall match {
          case WallPosition(`orientation`, `row`, otherColumn) => ((column - otherColumn).abs <= 1)
          case WallPosition(`orientation`, otherRow, `column`) => ((row - otherRow).abs <= 1)
          case WallPosition(orientation.opposite, otherRow, otherColumn) => (row == otherColumn) && (column == otherRow)
        }
    }
  }


  def existsPath(from: PawnPosition, to: Side, walls: Set[WallPosition]): Boolean = {
    var res = false
    val q = mutable.Queue.empty[PawnPosition]
    val used = mutable.Map.empty[PawnPosition, Boolean]
    while (q.nonEmpty) {
      val v = q.dequeue()
      if (isOnEdge(v, to)) {
        res = true
      }
      for (w <- adjacentPositions(from, walls)) {
        if (!used(w)) {
          used(w) = true
          q.enqueue(w)
        }
      }
    }

    res
  }

}
