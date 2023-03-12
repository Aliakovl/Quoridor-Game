package ru.quoridor.auth.store

import io.lettuce.core.RedisURI
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.model.User
import ru.utils.tagging.ID
import zio.{TaskLayer, ZLayer}

object RefreshTokenStore {
  val live: TaskLayer[RedisStore[ID[User], RefreshToken]] = ZLayer(
    RedisStore(RedisURI.create("redis://token-store:6379/0"), UUIDRedisCodec)
  )
}
