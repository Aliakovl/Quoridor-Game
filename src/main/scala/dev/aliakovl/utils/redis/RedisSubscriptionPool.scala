package dev.aliakovl.utils.redis

import dev.aliakovl.utils.pool.SubscriptionPool
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.pubsub.{RedisPubSubAdapter, RedisPubSubListener, StatefulRedisPubSubConnection}
import io.lettuce.core.support.AsyncPool
import zio.stream.SubscriptionRef
import zio.{RIO, Scope, Task, ZIO}

class RedisSubscriptionPool[K, V](
    pool: AsyncPool[StatefulRedisPubSubConnection[K, V]]
) extends SubscriptionPool[
      RedisPubSubAsyncCommands[K, V],
      SubscriptionRef[Option[V]]
    ]:
  override def withConnection(
      ref: SubscriptionRef[Option[V]]
  ): RIO[Scope, RedisPubSubAsyncCommands[K, V]] = {
    val listener = new RedisPubSubAdapter[K, V] {
      override def message(channel: K, message: V): Unit =
        zio.Unsafe.unsafe { unsafe ?=>
          zio.Runtime.default.unsafe
            .run {
              ref.set(Some(message))
            }
            .getOrThrowFiberFailure()
        }
    }

    ZIO
      .acquireRelease(acquirePubSubConnection(listener)) { connection =>
        ZIO.async[Any, Nothing, Unit] { cb =>
          connection.removeListener(listener)
          pool.release(connection).thenAcceptAsync { _ =>
            cb(ZIO.unit)
          }
        }
      }
      .map(_.async())
  }

  private def acquirePubSubConnection(
      listener: RedisPubSubListener[K, V]
  ): Task[StatefulRedisPubSubConnection[K, V]] =
    ZIO.async { cb =>
      pool.acquire.handleAsync { (connection, e) =>
        cb(
          ZIO
            .attempt(connection.addListener(listener))
            .as(connection)
        )
        cb(ZIO.fail(e))
      }
    }
