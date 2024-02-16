package dev.aliakovl.tbsg

trait Actions[+T[_], -State, Event]:
  def actions(state: State): T[Event]
