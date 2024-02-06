package dev.aliakovl.utils

trait StringParser[A]:
  def parse(from: String): Option[A]

object StringParser:
  def apply[A](using StringParser[A]): StringParser[A] = summon[StringParser[A]]
