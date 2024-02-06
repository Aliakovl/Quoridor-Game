package dev.aliakovl.quoridor.pubsub

import dev.aliakovl.quoridor.codec.redis.given
import dev.aliakovl.quoridor.config.Configuration
import dev.aliakovl.quoridor.model.{Game, GameResponse}
import dev.aliakovl.utils.pubsub.{Publisher, Subscriber}
import dev.aliakovl.utils.redis.{RedisPublisher, RedisSubscriber}
import dev.aliakovl.utils.redis.config.RedisConfig
import dev.aliakovl.utils.tagging.ID
import zio.stream.ZStream
import zio.{RIO, Scope, Task, TaskLayer, ZLayer}

class GamePubSubLive(
    publisher: Publisher[ID[Game], GameResponse],
    subscriber: Subscriber[ID[Game], GameResponse]
) extends GamePubSub:
  override def publish(game: GameResponse): Task[Unit] =
    publisher.publish(game.id, game)

  override def subscribe(
      gameId: ID[Game]
  ): RIO[Scope, ZStream[Any, Throwable, GameResponse]] =
    subscriber.subscribe(gameId)

object GamePubSubLive:
  val live: ZLayer[RedisConfig, Throwable, GamePubSub] =
    ZLayer.makeSome[RedisConfig, GamePubSub](
      RedisPublisher.live[ID[Game], GameResponse],
      RedisSubscriber.live[ID[Game], GameResponse],
      ZLayer.fromFunction(new GamePubSubLive(_, _))
    )

  val configuredLive: TaskLayer[GamePubSub] = Configuration.pubSubRedis >>> live
