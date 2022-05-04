package model.storage.InMemoryStorage

import model.{ProtoGame, User}
import model.storage.ProtoGameStorage

import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class ProtoGameStorageImpl extends ProtoGameStorage {

  override def insert(userId: UUID): Future[ProtoGame] = {
    val gameId = UUID.randomUUID()
    val pg = ProtoGame(gameId, Seq(User(userId)))
    protoGameStore.update(gameId, pg)
    Future.successful(pg)
  }

  override def find(id: UUID): Future[ProtoGame] = {
    protoGameStore.get(id) match {
      case Some(pg) => Future.successful(pg)
      case None => Future.failed(new IllegalArgumentException)
    }
  }

  override def update(id: UUID, userId: UUID): Future[ProtoGame] = {
    protoGameStore.get(id) match {
      case None => Future.failed(new IllegalArgumentException)
      case Some(pg) =>
        val users = User(userId) +: pg.users
        val newPg = pg.copy(users = users)
        protoGameStore.update(id, newPg)
        Future.successful(newPg)
    }
  }

  private val protoGameStore = TrieMap[UUID, ProtoGame]()
}