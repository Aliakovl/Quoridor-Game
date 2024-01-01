package dev.aliakovl.utils.pool

import zio.{RIO, Scope}

trait Pool[+C] extends SubscriptionPool[C, Nothing]:
  def withConnection: RIO[Scope, C]
  override def withConnection(ref: Nothing): RIO[Scope, C] = withConnection
