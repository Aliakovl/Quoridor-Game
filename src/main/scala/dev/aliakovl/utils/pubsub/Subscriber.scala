package dev.aliakovl.utils.pubsub

import zio.{RIO, Scope}
import zio.stream.ZStream

trait Subscriber[K, V]:
  def subscribe(channel: K): RIO[Scope, ZStream[Any, Throwable, V]]
