package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.model.GameException.*
import dev.aliakovl.quoridor.model.game.geometry.Side.*
import dev.aliakovl.quoridor.model.game.{Game, State}
import dev.aliakovl.quoridor.model.{ProtoGame, ProtoPlayer, ProtoPlayers, User}
import dev.aliakovl.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import dev.aliakovl.quoridor.model.game.geometry.Side
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.*
import zio.{Task, ZIO}

import java.util.UUID

class GameCreatorImpl(
    userDao: UserDao,
    protoGameDao: ProtoGameDao,
    gameDao: GameDao
) extends GameCreator {

  override def createGame(userId: ID[User]): Task[ProtoGame] = {
    lazy val gameId = UUID.randomUUID().tag[Game]
    val target = North
    userDao.findById(userId).flatMap { user =>
      protoGameDao
        .insert(gameId, userId, target)
        .as {
          val protoPlayer = ProtoPlayer(userId, user.username, target)
          ProtoGame(gameId, ProtoPlayers(protoPlayer, List.empty))
        }
    }
  }

  override def joinPlayer(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[ProtoGame] = {
    for {
      gameAlreadyStarted <- gameDao.hasStarted(gameId)
      _ <- ZIO.cond(
        !gameAlreadyStarted,
        (),
        GameAlreadyStartedException(gameId)
      )
      protoGame <- protoGameDao.find(gameId)
      playersNumber = protoGame.players.guests.size + 1
      _ <- ZIO.cond(playersNumber < 4, (), PlayersNumberLimitException)
      target = Side.values(playersNumber)
      _ <- protoGameDao.addPlayer(gameId, userId, target)
      user <- userDao.findById(userId)
      newPlayer = ProtoPlayer(user.id, user.username, target)
    } yield protoGame.copy(players =
      protoGame.players.copy(guests = protoGame.players.guests :+ newPlayer)
    )
  }

  override def startGame(gameId: ID[Game], userId: ID[User]): Task[Game] = {
    for {
      gameAlreadyStarted <- gameDao.hasStarted(gameId)
      _ <- ZIO.cond(
        !gameAlreadyStarted,
        (),
        GameAlreadyStartedException(gameId)
      )
      protoGame <- protoGameDao.find(gameId)
      _ <- ZIO.cond(
        protoGame.players.creator.id == userId,
        (),
        NotGameCreatorException(userId, gameId)
      )
      players <- ZIO.fromEither(protoGame.players.toPlayers)
      state = State(players, Set.empty)
      game <- gameDao.create(gameId, state)
    } yield game
  }
}
