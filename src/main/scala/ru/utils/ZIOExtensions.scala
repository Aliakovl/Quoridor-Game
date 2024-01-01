package ru.utils

import zio.ZIO

object ZIOExtensions:
  extension [R, E](zio: ZIO[R, E, Boolean])
    def orFail[E1 >: E](e: => E1): ZIO[R, E1, Unit] =
      ZIO.ifZIO(zio)(ZIO.unit, ZIO.fail(e))
