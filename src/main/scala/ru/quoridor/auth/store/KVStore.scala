package ru.quoridor.auth.store

import zio.Task

trait KVStore[K, V] {
  def add(key: K, value: V): Task[Long]
  def isMember(key: K, value: V): Task[Boolean]
  def remove(key: K, value: V): Task[Long]
}
