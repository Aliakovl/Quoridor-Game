package dev.aliakovl.tbsg

import dev.aliakovl.tbsg.quoridor.GameEvent.PawnMove
import dev.aliakovl.tbsg.quoridor.Side.Bottom
import dev.aliakovl.tbsg.quoridor.{Cell, PlayersCount, Quoridor}

object Main {
  def main(args: Array[String]): Unit = {
    val game = new Quoridor(9)
    val state = game.initialize(PlayersCount.FourPlayers)
    println(state)

    println(state.map(game.handleEvent(PawnMove(Bottom, Cell(7, 4)), _)))

  }
}
