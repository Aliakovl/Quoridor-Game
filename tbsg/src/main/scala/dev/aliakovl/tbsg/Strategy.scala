package dev.aliakovl.tbsg

trait Strategy[-State, +Event]:
  def decide(state: State): Option[Event]
