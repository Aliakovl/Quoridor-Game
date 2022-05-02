package model.game

import model.geometry._

import java.util.UUID


case class Player(id: UUID, pawnPosition: PawnPosition, wallsAmount: Int, target: Side)