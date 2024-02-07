package dev.aliakovl.tbsg

trait Rules[+F[_], +T[_], -Info, State, Event] {
  def initialize(info: Info): F[State]
  def handleEvent(event: Event, state: State): F[State]
  def permittedActions(state: State): T[Event]
}
