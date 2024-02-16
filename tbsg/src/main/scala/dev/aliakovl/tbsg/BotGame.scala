package dev.aliakovl.tbsg

import cats.FlatMap
import cats.syntax.all._

trait BotGame[F[_]: FlatMap, T[_], Info, Event, State](
    rules: Rules[F, Info, Event, State],
    strategy: StatelessStrategy[T, State, Event]
):
  def start(info: Info)(f: State => Unit): F[State] = {
    run(f)(rules.initialize(info))
  }

  private def run(f: State => Unit)(fs: F[State]): F[State] = {
    for {
      state <- fs
      _ = f(state)
      newState <- strategy.decide(state) match
        case None        => fs
        case Some(event) => run(f)(rules.handle(event, state))
    } yield newState
  }
end BotGame
