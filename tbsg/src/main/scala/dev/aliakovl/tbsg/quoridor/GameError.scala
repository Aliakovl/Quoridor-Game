package dev.aliakovl.tbsg.quoridor

enum GameError:
  case IllegalMoveInEndedGameError
  case OutOfBoardPawnMoveError
  case OutOfBoardPlaceWallError
  case PawnIllegalMoveError
  case AnotherPawnMoveError
  case WrongWaitingPawnsError
  case PlayerHasNotEnoughWallsToPlace
  case WallsIntersects
  case WallBlocksPathForPawn
