package ru.quoridor.engine.model.game.geometry

enum Orientation extends Opposite[Orientation] { self =>
  case Horizontal extends Orientation
  case Vertical extends Orientation

  override def opposite: Orientation = self match {
    case Orientation.Horizontal => Vertical
    case Orientation.Vertical   => Horizontal
  }
}
