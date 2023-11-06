package ru.quoridor.mq

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import zio.stream.ZStream
import zio.{Scope, ZIO, ZLayer}

trait GameUpdateSubscriber:
  def subscribe(
      gameId: ID[Game]
  ): ZIO[Scope, Throwable, ZStream[Any, Throwable, Game]]

class RedisGameUpdateSubscriber(hub: PubSubPattern[ID[Game], Game])
    extends GameUpdateSubscriber:
  override def subscribe(
      gameId: ID[Game]
  ): ZIO[Scope, Throwable, ZStream[Any, Throwable, Game]] =
    ZIO.succeed(hub.subscribe(gameId))

object GameUpdateSubscriber:
  val live
      : ZLayer[PubSubPattern[ID[Game], Game], Nothing, GameUpdateSubscriber] =
    ZLayer.fromFunction(new RedisGameUpdateSubscriber(_))
