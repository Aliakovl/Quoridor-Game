package ru.quoridor.auth.store.redis

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.support.*
import io.lettuce.core.{RedisClient, RedisURI, SetArgs}
import ru.quoridor.auth.store.KVStore
import ru.quoridor.config.TokenStore
import zio.*

import java.util.concurrent.CompletionStage
import java.util.function.Supplier

class RedisStore[K, V](
    private val pool: AsyncPool[StatefulRedisConnection[K, V]],
    private val ttl: Long
) extends KVStore[K, V] {
  override def set(key: K, value: V): Task[Boolean] = ZIO.scoped {
    withConnection.flatMap { async =>
      ZIO.async { cb =>
        async
          .set(key, value, SetArgs.Builder.nx().px(ttl))
          .handleAsync { (value: String, error: Throwable) =>
            cb(ZIO.succeed(value == "OK"))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  override def get(key: K): Task[Option[V]] = ZIO.scoped {
    withConnection.flatMap { async =>
      ZIO.async { cb =>
        async
          .get(key)
          .handleAsync[Unit] { (value: V, error: Throwable) =>
            cb(ZIO.succeed(Option(value)))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  override def delete(key: K): Task[Boolean] = ZIO.scoped {
    withConnection.flatMap { async =>
      ZIO.async { cb =>
        async
          .del(key)
          .handleAsync { (value: java.lang.Long, error: Throwable) =>
            cb(ZIO.succeed(value > 0))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  override def getDel(key: K): Task[Option[V]] = ZIO.scoped {
    withConnection.flatMap { async =>
      ZIO.async { cb =>
        async
          .getdel(key)
          .handleAsync { (value: V, error: Throwable) =>
            cb(ZIO.succeed(Option(value)))
            cb(ZIO.fail(error))
          }
      }
    }
  }

  private val acquire: Task[StatefulRedisConnection[K, V]] = ZIO.async { cb =>
    pool.acquire().handleAsync { (connection, e) =>
      cb(ZIO.succeed(connection))
      cb(ZIO.fail(e))
    }
  }

  private val withConnection: RIO[Scope, RedisAsyncCommands[K, V]] = {
    ZIO.acquireRelease(acquire) { connection =>
      ZIO.async[Any, Nothing, Unit] { cb =>
        pool.release(connection).thenAcceptAsync { _ =>
          cb(ZIO.unit)
        }
      }
    }
  }.map(_.async)
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
            new Supplier[CompletionStage[StatefulRedisConnection[K, V]]] {
              override def get()
                  : CompletionStage[StatefulRedisConnection[K, V]] =
                RedisClient.create().connectAsync(redisCodec, redisURI)
            },
            BoundedPoolConfig.create(),
            false
          )
          .handleAsync {
            (
                value: AsyncPool[StatefulRedisConnection[K, V]],
                error: Throwable
            ) =>
              cb(ZIO.succeed(new RedisStore(value, ttl.toMillis)))
              cb(ZIO.fail(error))
          }
      }
  }

  def live[K: Tag, V: Tag](using
      redisCodec: RedisCodec[K, V]
  ): RLayer[TokenStore, KVStore[K, V]] = ZLayer {
    ZIO.serviceWithZIO[TokenStore] {
      case TokenStore(host, port, databaseNumber, password, ttl) =>
        val uri = RedisURI.Builder
          .redis(host)
          .withPort(port)
          .withDatabase(databaseNumber)
          .withAuthentication("default", password)
          .build()
        RedisStore(uri, redisCodec, ttl)
    }
  }
}
