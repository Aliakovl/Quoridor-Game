package ru.quoridor.model.game.geometry

import Direction._

sealed trait Direction extends Cross

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
