package model.game.geometry


sealed trait Orientation extends Opposite[Orientation]

object Orientation {
  case object Horizontal extends Orientation {
    override val opposite: Orientation = Vertical
  }

  case object Vertical extends Orientation {
    override val opposite: Orientation = Horizontal
  }
}
