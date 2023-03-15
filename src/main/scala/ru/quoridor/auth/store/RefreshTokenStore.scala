package ru.quoridor.auth.store

import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.model.User
import ru.utils.ZIOExtensions.OrFail
import ru.utils.tagging.ID
import zio.{RLayer, Task, ZLayer}

trait RefreshTokenStore {
  def add(userId: ID[User], token: RefreshToken): Task[Unit]

  def remove(userId: ID[User], token: RefreshToken): Task[Unit]
}

class RefreshTokenStoreImpl(store: KVStore[RefreshToken, ID[User]])
    extends RefreshTokenStore {
  def add(userId: ID[User], token: RefreshToken): Task[Unit] = {
    store
      .set(token, userId)
      .orFail(new Throwable("maybe key is already set"))
  }

  def remove(userId: ID[User], token: RefreshToken): Task[Unit] = {
    store
      .delete(token, userId)
      .orFail(new Throwable("maybe key is already gone"))
  }
}

object RefreshTokenStore {
  val live: RLayer[KVStore[RefreshToken, ID[User]], RefreshTokenStore] =
    ZLayer.fromFunction(new RefreshTokenStoreImpl(_))
}
