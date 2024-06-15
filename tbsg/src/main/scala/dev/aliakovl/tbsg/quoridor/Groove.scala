package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.quoridor
import dev.aliakovl.tbsg.quoridor.Orientation.*

case class Groove(row: Int, column: Int, orientation: Orientation):
  def intersects(other: Groove): Boolean = other match {
    case Groove(`row`, `column`, _) => true
    case Groove(`row`, otherColumn, `orientation`)
        if orientation == Horizontal =>
      (column - otherColumn).abs <= 1
    case Groove(otherRow, `column`, `orientation`) if orientation == Vertical =>
      (row - otherRow).abs <= 1
    case _ => false
  }
end Groove
