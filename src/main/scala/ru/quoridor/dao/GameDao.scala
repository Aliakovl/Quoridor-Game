package ru.quoridor.dao

import ru.quoridor.model.{GamePreView, User}
import ru.quoridor.model.game.{Game, Move, State}
import ru.quoridor.dao.quill.QuillContext
import ru.quoridor.model.User.Userdata
import ru.utils.tagging.Id
import zio.{RLayer, Task, ZLayer}

trait GameDao {
  def find(gameId: Id[Game]): Task[Game]

  def find(gameId: Id[Game], step: Int): Task[Game]

  def lastStep(gameId: Id[Game]): Task[Int]

  def insert(
      gameId: Id[Game],
      step: Int,
      state: State,
      move: Move,
      winner: Option[Userdata]
  ): Task[Unit]

  def create(gameId: Id[Game], state: State): Task[Game]

  def hasStarted(gameId: Id[Game]): Task[Boolean]

  def history(userId: Id[User]): Task[List[Id[Game]]]

  def findParticipants(gameId: Id[Game]): Task[GamePreView]
}

object GameDao {
  val live: RLayer[QuillContext, GameDao] =
    ZLayer.fromFunction(new GameDaoImpl(_))
}
