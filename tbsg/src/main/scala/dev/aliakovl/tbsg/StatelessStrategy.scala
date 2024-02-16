package dev.aliakovl.tbsg

trait StatelessStrategy[T[_], -State, Event](using
    Actions[T, State, Event],
    Chooser[T, Event]
) extends Strategy[State, Event]:
  override def decide(state: State): Option[Event] =
    summon[Chooser[T, Event]].choose(
      summon[Actions[T, State, Event]].actions(state)
    )
end StatelessStrategy
