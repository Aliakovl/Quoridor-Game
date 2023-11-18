package ru.quoridor.pubsub.redis

import ru.quoridor.model.game.Game
import ru.quoridor.pubsub.GamePubSub
import ru.utils.pubsub.{Publisher, Subscriber}
import ru.utils.tagging.ID
import zio.stream.ZStream
import zio.{Scope, Task, ZIO}

class RedisGamePubSub(
    publisher: Publisher[ID[Game], Game],
    subscriber: Subscriber[ID[Game], Game]
) extends GamePubSub:
  override def publish(game: Game): Task[Unit] =
    publisher.publish(game.id, game)

  override def subscribe(
      gameId: ID[Game]
  ): ZIO[Scope, Throwable, ZStream[Any, Throwable, Game]] =
    ZIO.succeed(subscriber.subscribe(gameId))
