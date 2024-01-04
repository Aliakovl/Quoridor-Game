package dev.aliakovl.quoridor.dao.quill

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{RLayer, TaskLayer, ZLayer}

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

  val configuredLive: TaskLayer[QuillContext] =
    Quill.DataSource.fromPrefix("hikari") >>> live
