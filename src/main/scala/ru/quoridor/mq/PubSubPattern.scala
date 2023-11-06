package ru.quoridor.mq

import zio.{Scope, Task, ZIO}
import zio.stream.ZStream

trait PubSubPattern[K, V]:
  def publish(channel: K, message: V): Task[Unit]
  def subscribe(channel: K): ZStream[Any, Throwable, V]
