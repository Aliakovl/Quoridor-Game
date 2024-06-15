package dev.aliakovl.tbsg.quoridor

enum GameEvent:
  case PawnMove(pawnsEdge: Side, toCell: Cell)
  case PlaceWall(pawnsEdge: Side, toGroove: Groove)
