package model.storage.InMemoryStorage

import model.User
import model.storage.UserStorage

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class UserStorageImpl extends UserStorage {
  override def insert: Future[User] = {
    val id = UUID.randomUUID()
    val user = User(id)
    userStore.update(id, user)
    Future.successful(user)
  }

  override def find(id: UUID): Future[User] = {
    userStore.get(id) match {
      case Some(user) => Future.successful(user)
      case None => Future.failed(new IllegalArgumentException)
    }
  }

  private val userStore = TrieMap[UUID, User]()
}