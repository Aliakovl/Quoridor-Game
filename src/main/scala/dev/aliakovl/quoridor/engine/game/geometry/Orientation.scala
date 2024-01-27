package dev.aliakovl.quoridor.engine.game.geometry

enum Orientation extends Opposite[Orientation] { self =>
  case Horizontal extends Orientation
  case Vertical extends Orientation

  override def opposite: Orientation = self match {
    case Horizontal => Vertical
    case Vertical   => Horizontal
  }
}
