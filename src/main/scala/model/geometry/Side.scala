package model.geometry

sealed trait Side {
  val opposite: Side
}

object Side {
  object Top extends Side {
    override val opposite: Side = Bottom
  }
  object Bottom extends Side {
    override val opposite: Side = Top
  }
  object Left extends Side {
    override val opposite: Side = Right
  }
  object Right extends Side {
    override val opposite: Side = Left
  }

  val allSides: Seq[Side] = Seq(Top, Bottom, Left, Right)

}