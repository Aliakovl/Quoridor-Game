package dev.aliakovl.utils.pubsub

import zio.Task

trait Publisher[K, V]:
  def publish(channel: K, message: V): Task[Unit]
