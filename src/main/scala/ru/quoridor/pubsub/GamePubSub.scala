package ru.quoridor.pubsub

import ru.quoridor.codecs.redis.given
import ru.quoridor.config.PubSubRedis
import ru.quoridor.model.game.Game
import ru.quoridor.pubsub.redis.RedisGamePubSub
import ru.utils.pubsub.redis.{RedisPublisher, RedisSubscriber}
import ru.utils.pubsub.{Publisher, Subscriber}
import ru.utils.tagging.ID
import zio.{Scope, Task, ZIO, ZLayer}
import zio.stream.ZStream

trait GamePubSub:
  def publish(game: Game): Task[Unit]

  def subscribe(
      gameId: ID[Game]
  ): ZIO[Scope, Throwable, ZStream[Any, Throwable, Game]]

object GamePubSub:
  val live: ZLayer[PubSubRedis with Scope, Throwable, GamePubSub] =
    ZLayer.makeSome[PubSubRedis with Scope, GamePubSub](
      RedisPublisher.live[ID[Game], Game],
      RedisSubscriber.live[ID[Game], Game],
      ZLayer.fromFunction(new RedisGamePubSub(_, _))
    )
