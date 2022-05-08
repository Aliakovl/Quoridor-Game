package model.services

import cats.effect.Async
import cats.implicits._
import model.GameException.{NotEnoughPlayersException, PlayersNumberLimitException}
import model.game.geometry.{Board, Side}
import model.game.geometry.Side._
import model.{ProtoGame, ProtoPlayer}
import model.game.{Game, GameState, Player}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}

import java.util.UUID


class GameCreatorImpl[F[_]](protoGameStorage: ProtoGameStorage[F],
                                gameStorage: GameStorage[F],
                                userStorage: UserStorage[F])(implicit F: Async[F]) extends GameCreator[F] {

  override def createGame(userId: UUID): F[ProtoGame] = {
    protoGameStorage.insert(userId)
  }

  override def joinPlayer(gameId: UUID, userId: UUID): F[ProtoGame] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      playersNumber = protoGame.users.size
      _ <- if (playersNumber > 3) F.raiseError(PlayersNumberLimitException) else F.unit
      target = sides(protoGame.users.size)
      pg <- protoGameStorage.update(gameId, userId, target)
    } yield pg
  }

  override def startGame(gameId: UUID): F[Game] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      users = protoGame.users
      playersNumber = users.size
      _ <- if (playersNumber < 2) F.raiseError(NotEnoughPlayersException) else F.unit
      players = users.map{ case ProtoPlayer(id, login, target) =>
        Player(id, login, Board.initPosition(target.opposite), 21 / playersNumber, target)
      }
      firstTurnPlayer = players.head
      state = GameState(players.toSet, Set.empty)
      game <- gameStorage.create(gameId, firstTurnPlayer, state)
    } yield game
  }

  val sides: Seq[Side] = List(North, South, West, East)

}
