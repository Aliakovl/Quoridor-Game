package dev.aliakovl.quoridor.engine.game.geometry

trait Opposite[T]:
  extension (value: T) def opposite: T
