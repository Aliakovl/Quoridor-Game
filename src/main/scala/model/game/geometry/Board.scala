package model.game.geometry

import model.game.geometry.Direction._
import model.game.geometry.Side._

import scala.collection.mutable


object Board {
  private val width: Int = 9
  private val halfWidth: Int = width / 2

  def isPawnOnEdge(position: PawnPosition, side: Side): Boolean = {
    side match {
      case North => position.row == 0
      case South => position.row == width - 1
      case West  => position.column == 0
      case East  => position.column == width - 1
    }
  }

  def initPosition(side: Side): PawnPosition = {
    side match {
      case North => PawnPosition(0, halfWidth)
      case South => PawnPosition(width - 1, halfWidth)
      case West  => PawnPosition(halfWidth, 0)
      case East  => PawnPosition(halfWidth, width - 1)
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
    val newPosition = direction.step(pawnPosition)
    if (!wayIsBlocked && isPawnOnBoard(newPosition)) {
      Some(newPosition)
    } else {
      None
    }
  }

  def adjacentPositions(pawnPosition: PawnPosition,
                        walls: Set[WallPosition]): Seq[PawnPosition] = {
    Seq(
      adjacentPosition(_, _, ToNorth),
      adjacentPosition(_, _, ToSouth),
      adjacentPosition(_, _, ToWest),
      adjacentPosition(_, _, ToEast)
    ).flatMap(f => f(pawnPosition, walls))
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
        val oppositeOrientation = orientation.opposite
        otherWall match {
          case WallPosition(`orientation`, `row`, otherColumn) => (column - otherColumn).abs <= 1
          case WallPosition(`orientation`, otherRow, `column`) => (row - otherRow).abs <= 1
          case WallPosition(`oppositeOrientation`, otherRow, otherColumn) => (row == otherColumn) && (column == otherRow)
        }
    }
  }


  def existsPath(from: PawnPosition, target: Side, walls: Set[WallPosition]): Boolean = {
    val queue = mutable.Queue.empty[PawnPosition]
    val used = mutable.Set.empty[PawnPosition]
    queue.enqueue(from)
    used.add(from)
    while (queue.nonEmpty) {
      val position = queue.dequeue()
      if (isPawnOnEdge(position, target)) {
        return true
      }
      for (adjacentPosition <- adjacentPositions(position, walls)) {
        if (!used.contains(adjacentPosition)) {
          used.add(adjacentPosition)
          queue.enqueue(adjacentPosition)
        }
      }
    }
    false
  }

}
