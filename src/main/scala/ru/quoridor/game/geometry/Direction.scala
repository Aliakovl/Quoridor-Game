package ru.quoridor.game.geometry

import Orientation._
import Direction._

sealed trait Direction extends Cross with StepForward with WallConverter

object Direction {
  object ToNorth extends Direction
  object ToSouth extends Direction
  object ToWest extends Direction
  object ToEast extends Direction
}

trait Cross { this: Direction =>
  val crossed: (Direction, Direction) = {
    this match {
      case ToNorth | ToSouth => (ToWest, ToEast)
      case ToWest | ToEast   => (ToNorth, ToSouth)
    }
  }
}

trait StepForward { this: Direction =>
  def step(pawnPosition: PawnPosition): PawnPosition =
    this match {
      case ToNorth => pawnPosition.copy(row = pawnPosition.row - 1)
      case ToSouth => pawnPosition.copy(row = pawnPosition.row + 1)
      case ToWest  => pawnPosition.copy(column = pawnPosition.column - 1)
      case ToEast  => pawnPosition.copy(column = pawnPosition.column + 1)
    }
}

trait WallConverter { this: Direction =>
  def toWallPosition(pawnPosition: PawnPosition): WallPosition =
    pawnPosition match {
      case PawnPosition(row, column) =>
        this match {
          case ToNorth => WallPosition(Horizontal, row - 1, column)
          case ToSouth => WallPosition(Horizontal, row, column)
          case ToWest  => WallPosition(Vertical, column - 1, row)
          case ToEast  => WallPosition(Vertical, column, row)
        }
    }
}
