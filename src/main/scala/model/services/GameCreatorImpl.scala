package model.services

import cats.data.NonEmptyList
import cats.effect.Async
import cats.implicits._
import model.GameException.{NotEnoughPlayersException, PlayersNumberLimitException}
import model.game.geometry.Board
import model.game.geometry.Side._
import model.{ProtoGame, ProtoPlayer, User}
import model.game.{Game, GameState, Player, Players}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}
import utils.Typed.ID


class GameCreatorImpl[F[_]](protoGameStorage: ProtoGameStorage[F],
                                gameStorage: GameStorage[F],
                                userStorage: UserStorage[F])(implicit F: Async[F]) extends GameCreator[F] {

  override def createGame(userId: ID[User]): F[ProtoGame] = {
    protoGameStorage.insert(userId)
  }

  override def joinPlayer(gameId: ID[Game], userId: ID[User]): F[ProtoGame] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      playersNumber = protoGame.users.size
      _ <- if (playersNumber > 3) F.raiseError(PlayersNumberLimitException) else F.unit
      target = allSides(protoGame.users.size)
      pg <- protoGameStorage.update(gameId, userId, target)
    } yield pg
  }

  override def startGame(gameId: ID[Game]): F[Game] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      users = protoGame.users
      playersNumber = users.size
      _ <- if (playersNumber < 2) F.raiseError(NotEnoughPlayersException) else F.unit
      players = users.map{ case ProtoPlayer(id, login, target) =>
        Player(id, login, Board.initPosition(target.opposite), 21 / playersNumber, target)
      }
      firstTurnPlayer = players.head
      state = GameState(Players(firstTurnPlayer, NonEmptyList.fromListUnsafe(players.tail)), Set.empty) // TODO: UNSAFE
      game <- gameStorage.create(gameId, state)
    } yield game
  }
}
