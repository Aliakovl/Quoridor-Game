package ru.utils.redis

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.support.AsyncPool
import zio.{RIO, Scope, Task, ZIO}

class RedisPool[K, V](pool: AsyncPool[StatefulRedisConnection[K, V]])
    extends Pool[RedisAsyncCommands[K, V]]:
  private val acquire: Task[StatefulRedisConnection[K, V]] =
    ZIO.async { cb =>
      pool.acquire.handleAsync { (connection, e) =>
        cb(ZIO.succeed(connection))
        cb(ZIO.fail(e))
      }
    }

  override val withConnection: RIO[Scope, RedisAsyncCommands[K, V]] = {
    ZIO
      .acquireRelease(acquire) { connection =>
        ZIO.async[Any, Nothing, Unit] { cb =>
          pool.release(connection).thenAcceptAsync { _ =>
            cb(ZIO.unit)
          }
        }
      }
      .map(_.async())
  }
