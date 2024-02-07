package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.quoridor
import dev.aliakovl.tbsg.quoridor.Pawn.*

case class Pawns(activePawn: ActivePawn, waitingPawns: Set[WaitingPawn])
