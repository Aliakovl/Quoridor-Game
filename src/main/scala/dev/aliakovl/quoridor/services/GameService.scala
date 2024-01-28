package dev.aliakovl.quoridor.services

import dev.aliakovl.quoridor.engine.game.Move
import dev.aliakovl.quoridor.engine.game.geometry.{PawnPosition, WallPosition}
import dev.aliakovl.quoridor.model.{Game, GamePreView, GameResponse, User}
import dev.aliakovl.utils.tagging.ID
import zio.stream.ZStream
import zio.Task

trait GameService:
  def findGame(gameId: ID[Game]): Task[GameResponse]

  def subscribeOnGame(
      gameId: ID[Game]
  ): Task[ZStream[Any, Throwable, GameResponse]]

  def makeMove(
      gameId: ID[Game],
      userId: ID[User],
      move: Move
  ): Task[GameResponse]

  def usersHistory(userId: ID[User]): Task[List[GamePreView]]

  def gameHistory(gameId: ID[Game], userId: ID[User]): Task[List[GameResponse]]

  def possiblePawnMoves(
      gameId: ID[Game],
      userId: ID[User]
  ): Task[List[PawnPosition]]

  def possibleWallMoves(
      gameId: ID[Game]
  ): Task[Set[WallPosition]]
