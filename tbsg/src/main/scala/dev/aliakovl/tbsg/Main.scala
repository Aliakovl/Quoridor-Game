package dev.aliakovl.tbsg

import cats.syntax.all.*
import cats.FlatMap
import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import dev.aliakovl.tbsg.quoridor.*
import dev.aliakovl.tbsg.quoridor.SimpleQuoridorPrinter.stateStr

object Main {
  def main(args: Array[String]): Unit = {
    val quoridor = new Quoridor(9)
    val bot = new QuoridorBotGame(quoridor)

    val state = bot.start(PlayersCount.FourPlayers)(st => {
      System.out.println(stateStr(st))
      System.out.flush()
      Thread.sleep(100)
      System.out.print("\u001b[1\u001b[11A")
    })

    println(state)

  }
}
