package dev.aliakovl.utils.store

import zio.Task

trait KVStore[-K, V]:
  def set(key: K, value: V): Task[Boolean]
  def get(key: K): Task[Option[V]]
  def delete(key: K): Task[Boolean]
  def getDel(key: K): Task[Option[V]]
