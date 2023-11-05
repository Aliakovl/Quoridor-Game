package ru.quoridor.mq

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import zio.{Hub, UIO, ZIO, ZLayer}

trait GameUpdatePublisher:
  def publish(gameId: ID[Game]): UIO[Boolean]

object GameUpdatePublisher:
  class GameUpdatePublisherInMemImpl(hub: Hub[ID[Game]])
      extends GameUpdatePublisher:
    override def publish(gameId: ID[Game]): UIO[Boolean] =
      hub.publish(gameId)

  val InMemLive: ZLayer[Hub[ID[Game]], Nothing, GameUpdatePublisher] =
    ZLayer(ZIO.serviceWith[Hub[ID[Game]]] { hub =>
      new GameUpdatePublisherInMemImpl(hub)
    })
