package ru.utils

import zio.ZIO

object ZIOExtensions:
  extension [R, E](zio: ZIO[R, E, Boolean])
    def orFail[E1 >: E](e: => E1): ZIO[R, E1, Unit] =
      ZIO.ifZIO(zio)(ZIO.unit, ZIO.fail(e))

  extension [R, E, A](zio: ZIO[R, E, A])
    def withFilter(q: A => Boolean): ZIO[R, E, A] = zio.filterOrDieMessage(q)(
      "withFilter end up with runtime exception"
    )
