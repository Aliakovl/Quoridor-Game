package dev.aliakovl.tbsg.quoridor

import dev.aliakovl.tbsg.{Chooser, StatelessStrategy}

import scala.util.Random

final class QuoridorRandomBot private[quoridor] (using QuoridorActions)
    extends StatelessStrategy

object QuoridorRandomBot:
  def apply(actions: QuoridorActions): QuoridorRandomBot = {
    given QuoridorActions = actions
    new QuoridorRandomBot
  }
end QuoridorRandomBot

given Chooser[Set, GameEvent] with
  override def choose(from: Set[GameEvent]): Option[GameEvent] =
    Random.shuffle(from).headOption
