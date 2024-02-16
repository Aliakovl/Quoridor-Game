package dev.aliakovl.tbsg

trait Chooser[T[_], A]:
  def choose(from: T[A]): Option[A]
