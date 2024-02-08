package dev.aliakovl.tbsg

import dev.aliakovl.tbsg.quoridor.{PlayersCount, Quoridor}

object Main {
  def main(args: Array[String]): Unit = {
    val game = new Quoridor(9)
    println(game.initialize(PlayersCount.FourPlayers))
  }
}
