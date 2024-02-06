package dev.aliakovl.quoridor.dao

import dev.aliakovl.quoridor.engine.game.geometry.Side
import dev.aliakovl.quoridor.model.{Game, ProtoGame, User}
import dev.aliakovl.utils.tagging.ID
import zio.Task

trait ProtoGameDao:
  def find(gameId: ID[Game]): Task[ProtoGame]

  def insert(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]

  def addPlayer(gameId: ID[Game], userId: ID[User], target: Side): Task[Unit]
