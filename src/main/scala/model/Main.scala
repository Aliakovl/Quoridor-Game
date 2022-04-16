package game

import java.util.UUID

object Main {
  def main(args: Array[String]): Unit = {
    val players = Set(Player(UUID.randomUUID), Player(UUID.randomUUID))
    val a = Quoridor(players)
  }
}
