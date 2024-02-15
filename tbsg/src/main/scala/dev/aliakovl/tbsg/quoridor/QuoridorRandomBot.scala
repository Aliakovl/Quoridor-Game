package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.{Chooser, StatelessStrategy}
import dev.aliakovl.tbsg.quoridor.QuoridorRandomBot.given

import scala.util.Random

final class QuoridorRandomBot private[quoridor] (using QuoridorActions)
    extends StatelessStrategy[Set, GameState, GameEvent]

object QuoridorRandomBot:
  def apply(actions: QuoridorActions): QuoridorRandomBot = {
    given QuoridorActions = actions
    new QuoridorRandomBot
  }

  given Chooser[Set, GameEvent] with
    override def choose(from: Set[GameEvent]): Option[GameEvent] =
      Random.shuffle(from.toVector).headOption
end QuoridorRandomBot
