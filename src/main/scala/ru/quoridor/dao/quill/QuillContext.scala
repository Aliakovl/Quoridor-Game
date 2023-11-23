package ru.quoridor.dao.quill

import io.getquill.*
import io.getquill.jdbczio.Quill
import ru.quoridor.codec.postgres.PostgresExtensions
import zio.{RLayer, ZLayer}

import javax.sql.DataSource

class QuillContext(dataSource: DataSource)
    extends Quill.PostgresLite(
      NamingStrategy(SnakeCase, Escape),
      dataSource
    )
    with PostgresExtensions

object QuillContext:
  val live: RLayer[DataSource, QuillContext] =
    ZLayer.fromFunction(new QuillContext(_))
