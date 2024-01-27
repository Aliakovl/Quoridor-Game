package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.engine.game.Move
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, WallPosition}
import dev.aliakovl.quoridor.model.{GamePreView, User}
import dev.aliakovl.quoridor.model.game.Game
import dev.aliakovl.utils.tagging.ID
import zio.stream.ZStream
import zio.Task

trait GameService:
  def findGame(gameId: ID[Game]): Task[Game]

  def subscribeOnGame(
      gameId: ID[Game]
  ): Task[ZStream[Any, Throwable, Game]]

  def makeMove(gameId: ID[Game], userId: ID[User], move: Move): Task[Game]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]

  def gameHistory(gameId: ID[Game], userId: ID[User]): Task[List[Game]]

  def possiblePawnMoves(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[PawnPosition]]

  def possibleWallMoves(
      gameId: ID[Game]
  ): Task[Set[WallPosition]]
