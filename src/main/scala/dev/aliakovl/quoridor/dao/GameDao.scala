package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.model.{GamePreView, User}
import dev.aliakovl.quoridor.engine.{Game, Move, State}
import dev.aliakovl.quoridor.dao.quill.QuillContext
import dev.aliakovl.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

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

object GameDao:
  val live: RLayer[QuillContext, GameDao] =
    ZLayer.fromFunction(new GameDaoImpl(_))
