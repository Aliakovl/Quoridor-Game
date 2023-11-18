package ru.utils.pubsub

import zio.stream.ZStream

trait Subscriber[K, V]:
  def subscribe(channel: K): ZStream[Any, Throwable, V]
