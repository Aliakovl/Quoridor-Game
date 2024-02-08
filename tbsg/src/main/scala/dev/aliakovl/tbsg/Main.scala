package dev.aliakovl.tbsg

import dev.aliakovl.tbsg.quoridor.{PlayersCount, Quoridor}

object Main {
  def main(args: Array[String]): Unit = {
    val game = new Quoridor(9)

    val start = System.currentTimeMillis()
    val b = for {
      _ <- 0 until 10000000
      a = game.initialize(PlayersCount.FourPlayers)
    } yield a
    val end = System.currentTimeMillis()
    println(s"elapsed: ${(end - start) / 1000d}")

    println(b.lastOption)
  }
}
