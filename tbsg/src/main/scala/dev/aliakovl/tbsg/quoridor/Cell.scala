package dev.aliakovl.tbsg.quoridor

import com.softwaremill.quicklens.*
import dev.aliakovl.tbsg.quoridor.Orientation.{Horizontal, Vertical}

case class Cell(row: Int, column: Int):
  def step(in: Direction): Cell = in match {
    case Direction.Up    => this.modify(_.row)(_ - 1)
    case Direction.Down  => this.modify(_.row)(_ + 1)
    case Direction.Left  => this.modify(_.column)(_ - 1)
    case Direction.Right => this.modify(_.column)(_ + 1)
  }

  def grooves(from: Direction): (Groove, Groove) = from match {
    case Direction.Up =>
      (
        Groove(row - 1, column - 1, Horizontal),
        Groove(row - 1, column, Horizontal),
      )
    case Direction.Down =>
      (
        Groove(row, column - 1, Horizontal),
        Groove(row, column, Horizontal),
      )
    case Direction.Left =>
      (
        Groove(row - 1, column - 1, Vertical),
        Groove(row, column - 1, Vertical),
      )
    case Direction.Right =>
      (
        Groove(row - 1, column, Vertical),
        Groove(row, column, Vertical),
      )
  }
end Cell
