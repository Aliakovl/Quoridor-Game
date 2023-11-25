package ru.quoridor.pubsub

import ru.quoridor.engine.model.game.Game
import ru.utils.pubsub.{Publisher, Subscriber}
import ru.utils.tagging.ID
import zio.stream.ZStream
import zio.{RIO, Scope, Task}

class GamePubSubImpl(
    publisher: Publisher[ID[Game], Game],
    subscriber: Subscriber[ID[Game], Game]
) extends GamePubSub:
  override def publish(game: Game): Task[Unit] =
    publisher.publish(game.id, game)

  override def subscribe(
      gameId: ID[Game]
  ): RIO[Scope, ZStream[Any, Throwable, Game]] =
    subscriber.subscribe(gameId)
