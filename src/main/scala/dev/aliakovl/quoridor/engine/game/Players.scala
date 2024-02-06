package dev.aliakovl.quoridor.engine.game

import cats.data.NonEmptyList
import dev.aliakovl.quoridor.engine.GameInitializationException
import dev.aliakovl.quoridor.engine.GameInitializationException.*
import dev.aliakovl.quoridor.engine.game.geometry.{Board, Side}
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.utils.Shifting
import dev.aliakovl.utils.tagging.ID

case class Players(activePlayer: Player, enemies: NonEmptyList[Player]):
  lazy val toList: List[Player] = activePlayer +: enemies.toList

  def shift: Players = {
    Shifting[Player](activePlayer, enemies).shift match {
      case Shifting(p, pp) => Players(p, pp)
    }
  }

object Players:
  def toPlayers(creatorId: ID[User], creatorTarget: Side)(
      guests: List[(ID[User], Side)]
  ): Either[GameInitializationException, Players] = guests match {
    case Nil => Left(NotEnoughPlayersException)
    case head :: tail =>
      val n = tail.size + 2
      val ftoPlayer = toPlayer(n)
      if n > 4 then Left(PlayersNumberLimitException)
      else
        Right(
          Players(
            toPlayer(n)(creatorId, creatorTarget),
            NonEmptyList(ftoPlayer.tupled(head), tail.map(ftoPlayer.tupled))
          )
        )
  }

  private def toPlayer(playersNumber: Int)(id: ID[User], target: Side): Player =
    Player(
      id,
      Board.initPosition(target.opposite),
      21 / playersNumber,
      target
    )
