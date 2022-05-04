package model.storage

import model.User

import java.util.UUID
import scala.concurrent.Future

trait UserStorage {
  def find(id: UUID): Future[User]

  def insert: Future[User]
}
