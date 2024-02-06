package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.model.{Game, GameResponse, ProtoGame, User}
import dev.aliakovl.utils.tagging.ID
import zio.Task

trait GameCreator:
  def createGame(userId: ID[User]): Task[ProtoGame]

  def joinPlayer(gameId: ID[Game], playerId: ID[User]): Task[ProtoGame]

  def startGame(gameId: ID[Game], userId: ID[User]): Task[GameResponse]
