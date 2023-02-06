package ru.quoridor.model

import cats.data.NonEmptyList
import ru.quoridor.model.GameException.{
  NotEnoughPlayersException,
  PlayersNumberLimitException
}
import ru.quoridor.model.game.geometry.{Board, Side}
import ru.quoridor.model.game.{Player, Players}

import java.util.UUID

case class ProtoGame(gameId: UUID, players: ProtoPlayers)

case class ProtoPlayer(userId: UUID, login: String, target: Side) {
  def toUser: User = User(userId, login)
}

case class ProtoPlayers(creator: ProtoPlayer, guests: List[ProtoPlayer]) {
  lazy val toList: List[ProtoPlayer] = creator :: guests

  lazy val toPlayers: Either[GameException, Players] = {
    guests match {
      case Nil => Left(NotEnoughPlayersException)
      case head :: tail =>
        val n = tail.size + 2
        val toPlayerFun = toPlayer(n) _
        if (n > 4) {
          Left(PlayersNumberLimitException)
        } else {
          Right(
            Players(
              toPlayerFun(creator),
              NonEmptyList(toPlayerFun(head), tail.map(toPlayerFun))
            )
          )
        }
    }
  }

  private def toPlayer(playersNumber: Int)(protoPlayer: ProtoPlayer): Player =
    protoPlayer match {
      case ProtoPlayer(userId, login, target) =>
        Player(
          userId,
          login,
          Board.initPosition(target.opposite),
          21 / playersNumber,
          target
        )
    }
}
