package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.quoridor.Pawn.*

enum GameState:
  case ActiveGame(pawns: Pawns, walls: Set[Groove])
  case EndingGame(
      pawns: Pawns,
      walls: Set[Groove],
      winnersTable: List[NonActivePawn]
  )
  case EndedGame(walls: Set[Groove], winnersTable: List[NonActivePawn])
end GameState
