package model.storage

import model.{ProtoGame, User}

import java.util.UUID
import scala.concurrent.Future

trait ProtoGameStorage {
  def find(gameId: UUID): Future[ProtoGame]

  def insert(userId: UUID): Future[ProtoGame]

  def update(gameId: UUID, userId: UUID): Future[ProtoGame]
}