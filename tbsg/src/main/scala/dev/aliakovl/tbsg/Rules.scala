package dev.aliakovl.tbsg

trait Rules[+F[_], -Info, -Event, State]:
  def initialize(info: Info): F[State]
  def handle(event: Event, state: State): F[State]
