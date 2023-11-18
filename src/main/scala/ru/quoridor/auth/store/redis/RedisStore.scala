package ru.quoridor.auth.store.redis

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.support.*
import io.lettuce.core.{RedisClient, RedisURI, SetArgs}
import ru.quoridor.auth.store.KVStore
import ru.quoridor.config.TokenStore
import ru.utils.redis.RedisPool
import zio.*

import java.util.concurrent.CompletionStage
import java.util.function.Supplier

final class RedisStore[K, V](
    private val pool: RedisPool[K, V],
    private val ttl: Long
) extends KVStore[K, V] {
  override def set(key: K, value: V): Task[Boolean] = ZIO.scoped {
    pool.withConnection.flatMap { connection =>
      ZIO.async { cb =>
        connection
          .set(key, value, SetArgs.Builder.nx().px(ttl))
          .handleAsync { (value: String, error: Throwable) =>
            cb(ZIO.succeed(value == "OK"))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  override def get(key: K): Task[Option[V]] = ZIO.scoped {
    pool.withConnection.flatMap { connection =>
      ZIO.async { cb =>
        connection
          .get(key)
          .handleAsync[Unit] { (value: V, error: Throwable) =>
            cb(ZIO.succeed(Option(value)))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  override def delete(key: K): Task[Boolean] = ZIO.scoped {
    pool.withConnection.flatMap { connection =>
      ZIO.async { cb =>
        connection
          .del(key)
          .handleAsync { (value: java.lang.Long, error: Throwable) =>
            cb(ZIO.succeed(value > 0))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  override def getDel(key: K): Task[Option[V]] = ZIO.scoped {
    pool.withConnection.flatMap { connection =>
      ZIO.async { cb =>
        connection
          .getdel(key)
          .handleAsync { (value: V, error: Throwable) =>
            cb(ZIO.succeed(Option(value)))
            cb(ZIO.fail(error))
          }
      }
    }
  }
}

object RedisStore {
  def apply[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V],
      ttl: Duration
  ): Task[RedisStore[K, V]] = {
    ZIO
      .async { cb =>
        AsyncConnectionPoolSupport
          .createBoundedObjectPoolAsync(
            () => RedisClient.create().connectAsync(redisCodec, redisURI),
            BoundedPoolConfig.create(),
            false
          )
          .handleAsync {
            (
                value: AsyncPool[StatefulRedisConnection[K, V]],
                error: Throwable
            ) =>
              cb(
                ZIO.succeed(
                  new RedisStore(new RedisPool(value), ttl.toMillis)
                )
              )
              cb(ZIO.fail(error))
          }
      }
  }

  def live[K: Tag, V: Tag](using
      redisCodec: RedisCodec[K, V]
  ): RLayer[TokenStore, KVStore[K, V]] = ZLayer {
    ZIO.serviceWithZIO[TokenStore] {
      case TokenStore(host, port, database, password, ttl) =>
        val uri = RedisURI.Builder
          .redis(host)
          .withPort(port)
          .withDatabase(database)
          .withAuthentication("default", password)
          .build()
        RedisStore(uri, redisCodec, ttl)
    }
  }
}
