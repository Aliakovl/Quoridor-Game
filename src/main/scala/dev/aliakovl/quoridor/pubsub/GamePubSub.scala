package dev.aliakovl.quoridor.pubsub

import dev.aliakovl.quoridor.codec.redis.given
import dev.aliakovl.quoridor.config.PubSubRedis
import dev.aliakovl.quoridor.model.game.Game
import dev.aliakovl.utils.pubsub.redis.{RedisPublisher, RedisSubscriber}
import dev.aliakovl.utils.pubsub.{Publisher, Subscriber}
import dev.aliakovl.utils.tagging.ID
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
      ZLayer.fromFunction(new GamePubSubLive(_, _))
    )
