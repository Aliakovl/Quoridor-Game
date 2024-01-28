package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.GameException.*
import dev.aliakovl.quoridor.engine.game.geometry.Side.*
import dev.aliakovl.quoridor.model.*
import dev.aliakovl.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import dev.aliakovl.quoridor.engine.game.State
import dev.aliakovl.quoridor.engine.game.geometry.Side
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.*
import zio.{Task, URLayer, ZIO, ZLayer}

import java.util.UUID

class GameCreatorLive(
    userDao: UserDao,
    protoGameDao: ProtoGameDao,
    gameDao: GameDao
) extends GameCreator:
  override def createGame(userId: ID[User]): Task[ProtoGame] = {
    lazy val gameId = UUID.randomUUID().tag[Game]
    val target = South
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
      _ <- ZIO.when(gameAlreadyStarted)(
        ZIO.fail(GameAlreadyStartedException(gameId))
      )
      protoGame <- protoGameDao.find(gameId)
      playersNumber = protoGame.players.guests.size + 1
      _ <- ZIO.when(playersNumber >= 4)(
        ZIO.fail(PlayersNumberLimitException)
      )
      target = playersNumber match
        case 0 => South
        case 1 => North
        case 2 => West
        case 3 => East
      user <- userDao.findById(userId)
      _ <- protoGameDao.addPlayer(gameId, userId, target)
      newPlayer = ProtoPlayer(user.id, user.username, target)
    } yield protoGame.copy(players =
      protoGame.players.copy(guests = protoGame.players.guests :+ newPlayer)
    )
  }

  override def startGame(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[GameResponse] = {
    for {
      gameAlreadyStarted <- gameDao.hasStarted(gameId)
      _ <- ZIO.when(gameAlreadyStarted)(
        ZIO.fail(GameAlreadyStartedException(gameId))
      )
      protoGame <- protoGameDao.find(gameId)
      _ <- ZIO.when(userId != protoGame.players.creator.id)(
        ZIO.fail(NotGameCreatorException(userId, gameId))
      )
      players <- ZIO.fromEither(protoGame.players.toPlayers)
      state = State(players, Set.empty)
      game <- gameDao.create(gameId, state)
      users <- userDao.findUsers(players.toList.map(_.id))
    } yield GameResponse.fromGame(users)(game)
  }

object GameCreatorLive:
  val live: URLayer[
    UserDao & ProtoGameDao & GameDao,
    GameCreator
  ] =
    ZLayer.fromFunction(new GameCreatorLive(_, _, _))
