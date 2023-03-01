package ru.quoridor.storage.quill

import io.getquill.{Escape, NamingStrategy, SnakeCase}
import io.getquill.jdbczio.Quill
import zio.{RLayer, ZLayer}

import javax.sql.DataSource

class QuillContext(dataSource: DataSource)
    extends Quill.PostgresLite(
      NamingStrategy(SnakeCase, Escape),
      dataSource
    )
    with PostgresExtensions

object QuillContext {
  val live: RLayer[DataSource, QuillContext] =
    ZLayer.fromFunction(new QuillContext(_))
}
