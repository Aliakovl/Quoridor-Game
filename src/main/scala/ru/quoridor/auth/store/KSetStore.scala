package ru.quoridor.auth.store

import zio.Task

trait KSetStore[K, V] {
  def sadd(key: K, value: V*): Task[Long]
  def sismember(key: K, value: V): Task[Boolean]
  def srem(key: K, value: V*): Task[Long]
}
