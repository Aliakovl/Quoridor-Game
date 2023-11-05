package ru.quoridor.mq

import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import zio.stream.ZStream
import zio.{Hub, Scope, ZIO, ZLayer}

trait GameUpdateSubscriber:
  def subscribe: ZIO[Scope, Nothing, ZStream[Any, Nothing, ID[Game]]]

object GameUpdateSubscriber:
  class GameUpdateSubscriberInMemImpl(hub: Hub[ID[Game]])
      extends GameUpdateSubscriber:
    override def subscribe
        : ZIO[Scope, Nothing, ZStream[Any, Nothing, ID[Game]]] =
      ZStream.fromHubScoped(hub)

  val InMemLive: ZLayer[Hub[ID[Game]], Nothing, GameUpdateSubscriber] =
    ZLayer(ZIO.serviceWith[Hub[ID[Game]]] { hub =>
      new GameUpdateSubscriberInMemImpl(hub)
    })
