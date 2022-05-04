package model.game.geometry


sealed trait Side extends Opposite[Side]

object Side {
  object North extends Side {
    override val opposite: Side = South
  }

  object South extends Side {
    override val opposite: Side = North
  }

  object West extends Side {
    override val opposite: Side = East
  }

  object East extends Side {
    override val opposite: Side = West
  }

  val allSides: Seq[Side] = Seq(North, South, West, East)
}