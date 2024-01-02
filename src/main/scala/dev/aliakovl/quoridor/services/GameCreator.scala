package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.model.game.Game
import dev.aliakovl.quoridor.model.{ProtoGame, User}
import dev.aliakovl.quoridor.dao.{GameDao, ProtoGameDao, UserDao}
import dev.aliakovl.utils.tagging.ID
import zio.{Task, URLayer, ZLayer}

trait GameCreator:
  def createGame(userId: ID[User]): Task[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): Task[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): Task[Game]

object GameCreator:
  val live: URLayer[
    UserDao & ProtoGameDao & GameDao,
    GameCreator
  ] =
    ZLayer.fromFunction(new GameCreatorImpl(_, _, _))
