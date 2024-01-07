package dev.aliakovl.utils.redis

import dev.aliakovl.utils.pool.SubscriptionPool
import dev.aliakovl.utils.pubsub.Subscriber
import dev.aliakovl.utils.redis.config.PubSubRedis
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.support.{AsyncConnectionPoolSupport, AsyncPool, BoundedPoolConfig}
import io.lettuce.core.{RedisClient, RedisURI}
import izumi.reflect.Tag
import zio.*
import zio.stream.{SubscriptionRef, ZStream}

class RedisSubscriber[K, V](
    subscriptionPool: SubscriptionPool[
      RedisPubSubAsyncCommands[K, V],
      SubscriptionRef[Option[V]]
    ]
) extends Subscriber[K, V]:
  override def subscribe(channel: K): RIO[Scope, ZStream[Any, Throwable, V]] =
    for {
      ref <- SubscriptionRef.make[Option[V]](None)
      connection <- subscriptionPool.withConnection(ref)
      stream <- ZIO.async[Any, Throwable, ZStream[Any, Throwable, V]] { cb =>
        connection
          .subscribe(channel)
          .handleAsync { (_, error: Throwable) =>
            cb(
              ZIO.succeed(
                ref.changes.collectSome
              )
            )
            cb(ZIO.fail(error))
          }
      }
    } yield stream

object RedisSubscriber:
  def apply[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): Task[RedisSubscriber[K, V]] =
    subscriptionPool(redisURI, redisCodec)
      .map(new RedisSubscriptionPool(_))
      .map(new RedisSubscriber(_))

  def live[K: Tag, V: Tag](using
      redisCodec: RedisCodec[K, V]
  ): ZLayer[
    PubSubRedis,
    Throwable,
    Subscriber[K, V]
  ] = ZLayer(
    ZIO.serviceWithZIO[PubSubRedis] {
      case PubSubRedis(host, port, database, password) =>
        val uri = RedisURI.Builder
          .redis(host)
          .withPort(port)
          .withDatabase(database)
          .withAuthentication("default", password)
          .build()
        RedisSubscriber(uri, redisCodec)
    }
  )

  private def subscriptionPool[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): Task[AsyncPool[StatefulRedisPubSubConnection[K, V]]] = ZIO.async { cb =>
    AsyncConnectionPoolSupport
      .createBoundedObjectPoolAsync(
        () =>
          RedisClient
            .create()
            .connectPubSubAsync(redisCodec, redisURI),
        BoundedPoolConfig
          .builder()
          .minIdle(0)
          .maxIdle(1000)
          .maxTotal(1000)
          .build(),
        false
      )
      .handleAsync {
        (
            pool: AsyncPool[StatefulRedisPubSubConnection[K, V]],
            error: Throwable
        ) =>
          cb(ZIO.succeed(pool))
          cb(ZIO.fail(error))
      }
  }
