package dev.aliakovl.quoridor.pubsub

import dev.aliakovl.quoridor.model.{Game, GameResponse}
import dev.aliakovl.utils.tagging.ID
import zio.{RIO, Scope, Task}
import zio.stream.ZStream

trait GamePubSub:
  def publish(game: GameResponse): Task[Unit]

  def subscribe(
      gameId: ID[Game]
  ): RIO[Scope, ZStream[Any, Throwable, GameResponse]]
