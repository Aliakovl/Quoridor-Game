package ru.quoridor.pubsub

import ru.quoridor.codec.redis.given
import ru.quoridor.config.PubSubRedis
import ru.quoridor.engine.model.game.Game
import ru.utils.pubsub.redis.{RedisPublisher, RedisSubscriber}
import ru.utils.pubsub.{Publisher, Subscriber}
import ru.utils.tagging.ID
import zio.{RIO, Scope, Task, ZLayer}
import zio.stream.ZStream

trait GamePubSub:
  def publish(game: Game): Task[Unit]

  def subscribe(
      gameId: ID[Game]
  ): RIO[Scope, ZStream[Any, Throwable, Game]]

object GamePubSub:
  val live: ZLayer[PubSubRedis, Throwable, GamePubSub] =
    ZLayer.makeSome[PubSubRedis, GamePubSub](
      RedisPublisher.live[ID[Game], Game],
      RedisSubscriber.live[ID[Game], Game],
      ZLayer.fromFunction(new GamePubSubImpl(_, _))
    )
