package dev.aliakovl.tbsg.quoridor

import scala.language.implicitConversions
import scala.jdk.StreamConverters.*

object SimpleQuoridorPrinter {
  private val board: String =
    s"""| ⏐0⏐1⏐2⏐3⏐4⏐5⏐6⏐7⏐8⏐\u035F
        |0⏐                 ⏐\u035F
        |1⏐                 ⏐\u035F
        |2⏐                 ⏐\u035F
        |3⏐                 ⏐\u035F
        |4⏐                 ⏐\u035F
        |5⏐                 ⏐\u035F
        |6⏐                 ⏐\u035F
        |7⏐                 ⏐\u035F
        |8⏐                 ⏐
        | ⏐ ⏐ ⏐ ⏐ ⏐ ⏐ ⏐ ⏐ ⏐ ⏐""".stripMargin

  private def pawnStr(side: Side): String = side match {
    case Side.Bottom => "B"
    case Side.Left   => "L"
    case Side.Top    => "T"
    case Side.Right  => "R"
  }

  private def stateStr(pawns: Set[Pawn], walls: Set[Groove]): String = {
    board
      .lines()
      .toScala(List)
      .zipWithIndex
      .map { (line, r) =>
        line.zipWithIndex.map { (char, c) =>
          val row = r - 1
          if (r == 0) {
            if (c % 2 == 0 && c > 1 && c < 20) {
              s"$char\u035F"
            } else {
              char
            }
          } else if (c % 2 == 1) {
            val column = (c - 3) / 2
            if (
              walls.exists(w =>
                w.orientation == Orientation.Vertical && w.row == row && w.column == column
              )
            ) {
              "⏐"
            } else {
              char
            }
          } else {
            val column = (c - 2) / 2
            pawns.find(p =>
              p.position.row == row && p.position.column == column
            ) match {
              case None
                  if walls.exists(w =>
                    w.orientation == Orientation.Horizontal && w.row == row && w.column == column
                  ) =>
                "\u035F \u035F"
              case Some(pawn) =>
                val x = pawnStr(pawn.edge)
                if (
                  walls.exists(w =>
                    w.orientation == Orientation.Horizontal && w.row == row && w.column == column
                  )
                ) {
                  s"\u035F$x\u035F"
                } else {
                  x
                }
              case _ => char
            }
          }
        }.mkString
      }
      .mkString("\n")
  }

  def stateStr(state: GameState): String = {
    state match
      case GameState.ActiveGame(pawns, walls, winnersTable) =>
        stateStr(pawns.toSet ++ winnersTable.toSet, walls)
      case GameState.EndedGame(walls, winnersTable) =>
        stateStr(winnersTable.toSet, walls)
  }
}
