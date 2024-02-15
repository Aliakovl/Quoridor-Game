package dev.aliakovl.tbsg.quoridor

import cats.FlatMap
import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import dev.aliakovl.tbsg.BotGame

final class QuoridorBotGame(
    quoridor: Quoridor
) extends BotGame[
      [A] =>> ValidatedNec[GameError, A],
      Set,
      PlayersCount,
      GameEvent,
      GameState
    ](quoridor.rules, QuoridorRandomBot(quoridor.actions)) {}

given [E]: FlatMap[[A] =>> Validated[NonEmptyChain[E], A]] =
  new FlatMap[[A] =>> Validated[NonEmptyChain[E], A]]:
    override def tailRecM[A, B](a: A)(
        f: A => ValidatedNec[E, Either[A, B]]
    ): ValidatedNec[E, B] = ???

    override def flatMap[A, B](fa: Validated[NonEmptyChain[E], A])(
        f: A => Validated[NonEmptyChain[E], B]
    ): Validated[NonEmptyChain[E], B] = fa.andThen(f)

    override def map[A, B](fa: Validated[NonEmptyChain[E], A])(
        f: A => B
    ): Validated[NonEmptyChain[E], B] = fa.map(f)
