package ru.quoridor.model.game.geometry

import ru.quoridor.model.game.geometry.Direction._
import ru.quoridor.model.game.geometry.Orientation._

final case class PawnPosition(row: Int, column: Int) {
  def step(direction: Direction): PawnPosition = {
    direction match {
      case ToNorth => this.copy(row = this.row - 1)
      case ToSouth => this.copy(row = this.row + 1)
      case ToWest  => this.copy(column = this.column - 1)
      case ToEast  => this.copy(column = this.column + 1)
    }
  }

  def wall(direction: Direction): WallPosition = {
    this match {
      case PawnPosition(row, column) =>
        direction match {
          case ToNorth => WallPosition(Horizontal, row - 1, column)
          case ToSouth => WallPosition(Horizontal, row, column)
          case ToWest  => WallPosition(Vertical, column - 1, row)
          case ToEast  => WallPosition(Vertical, column, row)
        }
    }
  }
}
