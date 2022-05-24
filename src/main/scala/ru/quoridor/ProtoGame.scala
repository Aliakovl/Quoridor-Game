package ru.quoridor

import cats.data.NonEmptyList
import GameException.{NotEnoughPlayersException, PlayersNumberLimitException}
import ru.quoridor.game.geometry.{Board, Side}
import ru.quoridor.game.{Game, Player, Players}
import ru.utils.Typed.ID


case class ProtoGame(id: ID[Game], players: ProtoPlayers)

case class ProtoPlayer(id: ID[User], login: String, target: Side) {
  def toUser: User = User(id, login)
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

  private def toPlayer(playersNumber: Int)(protoPlayer: ProtoPlayer): Player = protoPlayer match {
    case ProtoPlayer(id, login, target) =>
      game.Player(id, login, Board.initPosition(target.opposite), 21 / playersNumber, target)
  }
}
