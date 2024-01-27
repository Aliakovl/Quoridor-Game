package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.engine.game.{Move, State}
import dev.aliakovl.quoridor.model.{Game, GamePreView, User}
import dev.aliakovl.utils.tagging.ID
import zio.Task

trait GameDao:
  def find(gameId: ID[Game]): Task[Game]

  def find(gameId: ID[Game], step: Int): Task[Game]

  def lastStep(gameId: ID[Game]): Task[Int]

  def insert(
      gameId: ID[Game],
      step: Int,
      state: State,
      move: Move,
      winner: Option[User]
  ): Task[Unit]

  def create(gameId: ID[Game], state: State): Task[Game]

  def hasStarted(gameId: ID[Game]): Task[Boolean]

  def history(userId: ID[User]): Task[List[ID[Game]]]

  def findParticipants(gameId: ID[Game]): Task[GamePreView]
