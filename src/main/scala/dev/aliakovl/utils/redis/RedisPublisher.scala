package dev.aliakovl.utils.redis

import dev.aliakovl.utils.pool.Pool
import dev.aliakovl.utils.pubsub.Publisher
import dev.aliakovl.utils.redis.config.RedisConfig
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.support.*
import io.lettuce.core.{RedisClient, RedisURI}
import izumi.reflect.Tag
import zio.*

class RedisPublisher[K, V](
    publishingPool: Pool[RedisAsyncCommands[K, V]]
) extends Publisher[K, V]:
  override def publish(channel: K, message: V): Task[Unit] = ZIO.scoped {
    publishingPool.withConnection.flatMap { connection =>
      ZIO.async { cb =>
        connection
          .publish(channel, message)
          .handleAsync { (_: java.lang.Long, error: Throwable) =>
            cb(ZIO.unit)
            cb(ZIO.fail(error))
          }
      }
    }
  }

object RedisPublisher:
  def apply[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): Task[RedisPublisher[K, V]] =
    publishingPool(redisURI, redisCodec)
      .map(new RedisPool(_))
      .map(new RedisPublisher(_))

  def live[K: Tag, V: Tag](using
      redisCodec: RedisCodec[K, V]
  ): ZLayer[
    RedisConfig,
    Throwable,
    Publisher[K, V]
  ] = ZLayer(
    ZIO.serviceWithZIO[RedisConfig] {
      case RedisConfig(host, port, database, password, _) =>
        val uri = RedisURI.Builder
          .redis(host)
          .withPort(port)
          .withDatabase(database)
          .withAuthentication("default", password)
          .build()
        RedisPublisher(uri, redisCodec)
    }
  )

  private def publishingPool[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): Task[AsyncPool[StatefulRedisConnection[K, V]]] = ZIO.async { cb =>
    AsyncConnectionPoolSupport
      .createBoundedObjectPoolAsync(
        () => RedisClient.create().connectAsync(redisCodec, redisURI),
        BoundedPoolConfig
          .builder()
          .minIdle(0)
          .maxIdle(8)
          .maxTotal(8)
          .build(),
        false
      )
      .handleAsync {
        (
            pool: AsyncPool[StatefulRedisConnection[K, V]],
            error: Throwable
        ) =>
          cb(ZIO.succeed(pool))
          cb(ZIO.fail(error))
      }
  }
