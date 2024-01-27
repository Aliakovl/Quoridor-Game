package dev.aliakovl.quoridor.model

import dev.aliakovl.quoridor.GameException.{
  NotEnoughPlayersException,
  PlayersNumberLimitException
}
import cats.data.NonEmptyList
import dev.aliakovl.quoridor.GameException
import dev.aliakovl.quoridor.engine.game.{Player, Players}
import dev.aliakovl.quoridor.engine.game.geometry.Board
import sttp.tapir.generic.auto.*
import sttp.tapir.Schema

case class ProtoPlayers(creator: ProtoPlayer, guests: List[ProtoPlayer]) {
  lazy val toList: List[ProtoPlayer] = creator :: guests

  lazy val toPlayers: Either[GameException, Players] = {
    guests match {
      case Nil => Left(NotEnoughPlayersException)
      case head :: tail =>
        val n = tail.size + 2
        val toPlayerFun = toPlayer(n) _
        if n > 4 then Left(PlayersNumberLimitException)
        else
          Right(
            Players(
              toPlayerFun(creator),
              NonEmptyList(toPlayerFun(head), tail.map(toPlayerFun))
            )
          )
    }
  }

  private def toPlayer(playersNumber: Int)(protoPlayer: ProtoPlayer): Player =
    protoPlayer match {
      case ProtoPlayer(id, username, target) =>
        Player(
          id,
          username,
          Board.initPosition(target.opposite),
          21 / playersNumber,
          target
        )
    }
}

object ProtoPlayers:
  given Schema[ProtoPlayers] = Schema.derivedSchema[ProtoPlayers]
