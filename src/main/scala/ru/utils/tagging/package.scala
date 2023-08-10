package ru.utils

import java.util.UUID

package object tagging {
  type Id[B] = Tagged[UUID, B]
  type @@[A, B] = Tagged[A, B]
}
