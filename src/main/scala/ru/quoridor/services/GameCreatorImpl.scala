package ru.quoridor.services

import ru.quoridor.GameException._
import ru.quoridor.game.geometry.Side._
import ru.quoridor.game.{Game, State}
import ru.quoridor.{ProtoGame, User}
import ru.quoridor.storage.{GameStorage, ProtoGameStorage}
import ru.utils.Typed.ID
import zio.{Task, ZIO}

class GameCreatorImpl(
    protoGameStorage: ProtoGameStorage,
    gameStorage: GameStorage
) extends GameCreator {

  override def createGame(userId: ID[User]): Task[ProtoGame] = {
    protoGameStorage.insert(userId)
  }

  override def joinPlayer(
      gameId: ID[Game],
      userId: ID[User]
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

  override def startGame(gameId: ID[Game], userId: ID[User]): Task[Game] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      _ <- ZIO.cond(
        protoGame.players.creator.id == userId,
        (),
        NotGameCreatorException(userId, gameId)
      )
      players <- ZIO.fromEither(protoGame.players.toPlayers)
      state = State(players, Set.empty)
      game <- gameStorage.create(gameId, state)
    } yield game
  }
}
