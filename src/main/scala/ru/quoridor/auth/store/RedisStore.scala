package ru.quoridor.auth.store

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.{RedisClient, RedisURI}
import zio.{Task, ZIO}

class RedisStore[K, V](private val connection: StatefulRedisConnection[K, V])
    extends KSetStore[K, V] {

  private val async: RedisAsyncCommands[K, V] = connection.async()

  override def sadd(key: K, value: V*): Task[Long] = {
    ZIO.async { cb =>
      async
        .sadd(key, value: _*)
        .handleAsync { (value: java.lang.Long, error: Throwable) =>
          cb(ZIO.succeed(value))
          cb(ZIO.fail(error))
        }
    }
  }

  override def sismember(key: K, value: V): Task[Boolean] = {
    ZIO.async { cb =>
      async
        .sismember(key, value)
        .handleAsync[Unit] { (value: java.lang.Boolean, error: Throwable) =>
          cb(ZIO.succeed(value))
          cb(ZIO.fail(error))
        }
    }
  }

  override def srem(key: K, value: V*): Task[Long] = {
    ZIO.async { cb =>
      async
        .srem(key, value: _*)
        .handleAsync { (value: java.lang.Long, error: Throwable) =>
          cb(ZIO.succeed(value))
          cb(ZIO.fail(error))
        }
    }
  }
}

object RedisStore {
  def apply[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): ZIO[Any, Throwable, RedisStore[K, V]] = {
    ZIO
      .async { cb =>
        RedisClient.create
          .connectAsync(redisCodec, redisURI)
          .handleAsync {
            (value: StatefulRedisConnection[K, V], error: Throwable) =>
              cb(ZIO.succeed(new RedisStore(value)))
              cb(ZIO.fail(error))
          }
      }
  }
}
