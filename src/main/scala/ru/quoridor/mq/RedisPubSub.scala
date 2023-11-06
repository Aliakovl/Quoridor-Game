package ru.quoridor.mq

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.{RedisClient, RedisURI}
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.lettuce.core.resource.{ClientResources, Delay}
import io.lettuce.core.support.{
  AsyncConnectionPoolSupport,
  AsyncPool,
  BoundedPoolConfig
}
import ru.quoridor.config.TokenStore
import ru.utils.redis.{Pool, RedisPool, RedisSubscriptionPool, SubscriptionPool}
import zio.*
import zio.stream.{SubscriptionRef, ZStream}

import java.util.concurrent.CompletionStage
import java.util.function.Supplier

class RedisPubSub[K, V](
    publishingPool: Pool[RedisAsyncCommands[K, V]],
    subscriptionPool: SubscriptionPool[
      RedisPubSubAsyncCommands[K, V],
      SubscriptionRef[
        Option[V]
      ]
    ]
) extends PubSubPattern[K, V] {
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

  override def subscribe(
      channel: K
  ): ZStream[Any, Throwable, V] = ZStream.unwrapScoped(
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
  )
}

object RedisPubSub {
  def apply[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): RIO[Scope, RedisPubSub[K, V]] = for {
    publishingPool <- publishingPool(redisURI, redisCodec)
    subscriptionPool <- subscriptionPool(redisURI, redisCodec)
  } yield new RedisPubSub(
    new RedisPool(publishingPool),
    new RedisSubscriptionPool(subscriptionPool)
  )

  private def publishingPool[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): Task[AsyncPool[StatefulRedisConnection[K, V]]] = {
    ZIO.async { cb =>
      AsyncConnectionPoolSupport
        .createBoundedObjectPoolAsync(
          () => RedisClient.create().connectAsync(redisCodec, redisURI),
          BoundedPoolConfig.builder().minIdle(0).maxIdle(8).maxTotal(8).build(),
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
  }

  private val subClientResources: ClientResources = ClientResources
    .builder()
    .reconnectDelay(Delay.constant(250.millis.asJava))
    .build()

  private def subscriptionPool[K, V](
      redisURI: RedisURI,
      redisCodec: RedisCodec[K, V]
  ): Task[AsyncPool[StatefulRedisPubSubConnection[K, V]]] = {
    ZIO.async { cb =>
      AsyncConnectionPoolSupport
        .createBoundedObjectPoolAsync(
          () => {
            RedisClient
              .create(subClientResources)
              .connectPubSubAsync(redisCodec, redisURI)
          },
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
  }

  def live[K: Tag, V: Tag](using
      redisCodec: RedisCodec[K, V]
  ): RLayer[TokenStore with Scope, PubSubPattern[K, V]] = ZLayer {
    ZIO.serviceWithZIO[TokenStore] {
      case TokenStore(host, port, _, password, _) =>
        val uri = RedisURI.Builder
          .redis(host)
          .withPort(port)
          .withDatabase(1)
          .withAuthentication("default", password)
          .build()
        RedisPubSub(uri, redisCodec)
    }
  }
}
