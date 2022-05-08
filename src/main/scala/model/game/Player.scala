package model.game

import model.game.geometry.{PawnPosition, Side}

import java.util.UUID

case class Player(id: UUID, login: String, pawnPosition: PawnPosition, wallsAmount: Int, target: Side)
