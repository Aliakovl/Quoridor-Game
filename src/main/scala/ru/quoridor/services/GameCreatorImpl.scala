package ru.quoridor.services

import ru.quoridor.model.GameException._
import ru.quoridor.model.game.geometry.Side._
import ru.quoridor.model.game.{Game, State}
import ru.quoridor.model.ProtoGame
import ru.quoridor.storage.{GameStorage, ProtoGameStorage}
import zio.{Task, ZIO}

import java.util.UUID

class GameCreatorImpl(
    protoGameStorage: ProtoGameStorage,
    gameStorage: GameStorage
) extends GameCreator {

  override def createGame(userId: UUID): Task[ProtoGame] = {
    protoGameStorage.insert(userId)
  }

  override def joinPlayer(
      gameId: UUID,
      userId: UUID
  ): Task[ProtoGame] = {
    for {
      gameAlreadyStarted <- gameStorage.exists(gameId)
      _ <- ZIO.cond(
        !gameAlreadyStarted,
        (),
        GameAlreadyStartedException(gameId)
      )
      protoGame <- protoGameStorage.find(gameId)
      playersNumber = protoGame.players.guests.size + 1
      _ <- ZIO.cond(playersNumber < 4, (), PlayersNumberLimitException)
      target = allSides(playersNumber)
      pg <- protoGameStorage.update(gameId, userId, target)
    } yield pg
  }

  override def startGame(gameId: UUID, userId: UUID): Task[Game] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      _ <- ZIO.cond(
        protoGame.players.creator.userId == userId,
        (),
        NotGameCreatorException(userId, gameId)
      )
      players <- ZIO.fromEither(protoGame.players.toPlayers)
      state = State(players, Set.empty)
      game <- gameStorage.create(gameId, state)
    } yield game
  }
}

object GameCreatorImpl {
  def apply(
      protoGameStorage: ProtoGameStorage,
      gameStorage: GameStorage
  ): GameCreatorImpl = new GameCreatorImpl(protoGameStorage, gameStorage)
}
