package dev.aliakovl.tbsg.quoridor

enum Side:
  case Bottom, Left, Top, Right

object Side:
  given Ordering[Side] = Ordering.by(_.ordinal)
