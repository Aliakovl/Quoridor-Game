package ru.quoridor.model.game.geometry

import Orientation._
import Direction._
import monocle.Monocle._

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
      case ToNorth => pawnPosition.focus(_.row).modify(_ - 1)
      case ToSouth => pawnPosition.focus(_.row).modify(_ + 1)
      case ToWest  => pawnPosition.focus(_.column).modify(_ - 1)
      case ToEast  => pawnPosition.focus(_.column).modify(_ + 1)
    }
}

trait WallConverter { this: Direction =>
  val toWallPosition: PawnPosition => WallPosition = {
    case PawnPosition(row, column) =>
      this match {
        case ToNorth => WallPosition(Horizontal, row - 1, column)
        case ToSouth => WallPosition(Horizontal, row, column)
        case ToWest  => WallPosition(Vertical, column - 1, row)
        case ToEast  => WallPosition(Vertical, column, row)
      }
  }
}
