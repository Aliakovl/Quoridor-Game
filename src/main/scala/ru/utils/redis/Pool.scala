package ru.utils.redis

import zio.{RIO, Scope}

trait Pool[+C] extends SubscriptionPool[C, Nothing]:
  def withConnection: RIO[Scope, C]
  override def withConnection(ref: Nothing): RIO[Scope, C] = withConnection
