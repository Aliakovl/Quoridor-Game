package ru.utils.pool

import zio.{RIO, Scope}

trait SubscriptionPool[+C, -R]:
  def withConnection(ref: R): RIO[Scope, C]
