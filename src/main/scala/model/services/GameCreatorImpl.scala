package model.services

import cats.effect.Async
import cats.implicits._
import model.GameException.{GameAlreadyStarted, NotGameCreator, PlayersNumberLimitException}
import model.game.geometry.Side._
import model.{ProtoGame, User}
import model.game.{Game, State}
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
      gameAlreadyStarted <- gameStorage.exists(gameId)
      _ <- F.fromEither(Either.cond(!gameAlreadyStarted, (), GameAlreadyStarted(gameId)))
      protoGame <- protoGameStorage.find(gameId)
      playersNumber = protoGame.players.guests.size + 1
      _ <- if (playersNumber > 3) F.raiseError(PlayersNumberLimitException) else F.unit
      target = allSides(playersNumber)
      pg <- protoGameStorage.update(gameId, userId, target)
    } yield pg
  }

  override def startGame(gameId: ID[Game], userId: ID[User]): F[Game] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      _ <- F.fromEither(Either.cond(protoGame.players.creator.id == userId, (), NotGameCreator(userId, gameId)))
      players <- F.fromEither(protoGame.players.toPlayers)
      state = State(players, Set.empty)
      game <- gameStorage.create(gameId, state)
    } yield game
  }
}
