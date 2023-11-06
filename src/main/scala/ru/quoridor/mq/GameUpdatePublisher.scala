package ru.quoridor.mq

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import zio.{Task, ZLayer}

trait GameUpdatePublisher:
  def publish(game: Game): Task[Unit]

class RedisGameUpdatePublisher(hub: PubSubPattern[ID[Game], Game])
    extends GameUpdatePublisher:
  override def publish(game: Game): Task[Unit] =
    hub.publish(game.id, game)

object GameUpdatePublisher:
  val live
      : ZLayer[PubSubPattern[ID[Game], Game], Nothing, GameUpdatePublisher] =
    ZLayer.fromFunction(new RedisGameUpdatePublisher(_))
