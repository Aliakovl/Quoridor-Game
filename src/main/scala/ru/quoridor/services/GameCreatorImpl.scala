package ru.quoridor.services

import ru.quoridor.model.GameException._
import ru.quoridor.model.game.geometry.Side._
import ru.quoridor.model.game.{Game, State}
import ru.quoridor.model.{ProtoGame, ProtoPlayer, ProtoPlayers, User}
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.Implicits.TaggedOps
import zio.{Task, ZIO}

import java.util.UUID

class GameCreatorImpl(
    userStorage: UserStorage,
    protoGameStorage: ProtoGameStorage,
    gameStorage: GameStorage
) extends GameCreator {

  override def createGame(userId: ID[User]): Task[ProtoGame] = {
    lazy val gameId = UUID.randomUUID().tag[Game]
    val target = North
    userStorage.find(userId).flatMap { user =>
      protoGameStorage
        .insert(gameId, userId)
        .as {
          val protoPlayer = ProtoPlayer(userId, user.login, target)
          ProtoGame(gameId, ProtoPlayers(protoPlayer, List.empty))
        }
    }
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
