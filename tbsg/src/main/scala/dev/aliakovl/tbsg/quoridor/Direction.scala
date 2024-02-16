package dev.aliakovl.tbsg.quoridor

enum Direction:
  case Up, Down, Left, Right

  def crossed: (Direction, Direction) = this match {
    case Direction.Up | Direction.Down    => (Direction.Left, Direction.Right)
    case Direction.Left | Direction.Right => (Direction.Up, Direction.Down)
  }
end Direction
