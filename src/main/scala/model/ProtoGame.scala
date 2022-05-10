package model

import cats.data.NonEmptyList
import model.GameException.{NotEnoughPlayersException, PlayersNumberLimitException}
import model.game.{Game, Player, Players}
import model.game.geometry.{Board, Side}
import utils.Typed.ID


case class ProtoGame(gameId: ID[Game], protoPlayers: ProtoPlayers)

case class ProtoPlayer(userId: ID[User], login: String, target: Side)

case class ProtoPlayers(creator: ProtoPlayer, guests: List[ProtoPlayer]) {
  lazy val toList: List[ProtoPlayer] = creator :: guests

  lazy val toPlayers: Either[GameException, Players] = {
    guests match {
      case Nil => Left(throw NotEnoughPlayersException)
      case head :: tail =>
        val n = tail.size
        val toPlayerFun = toPlayer(n) _
        if (n > 3) {
          Left(throw PlayersNumberLimitException)
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
      Player(id, login, Board.initPosition(target.opposite), 21 / playersNumber, target)
  }
}
