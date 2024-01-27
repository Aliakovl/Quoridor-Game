package dev.aliakovl.quoridor.dao.quill

import cats.Show
import dev.aliakovl.quoridor.codec.string.given
import dev.aliakovl.quoridor.engine.game.geometry.{Orientation, Side}
import dev.aliakovl.utils.StringParser
import dev.aliakovl.utils.tagging.Tagged
import dev.aliakovl.utils.tagging.Tagged.*
import io.getquill.MappedEncoding
import io.getquill.context.jdbc.JdbcContextTypes
import org.postgresql.util.PGobject

import java.sql.Types

trait PostgresExtensions:
  this: JdbcContextTypes[_, _] =>
  given Encoder[Side] =
    encoder[Side](
      Types.OTHER,
      (index: Index, value: Side, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("side")
        pgObj.setValue(Show[Side].show(value))
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  given Decoder[Side] =
    decoder[Side] { row => index =>
      StringParser[Side].parse(row.getString(index)).get
    }

  given Encoder[Orientation] =
    encoder[Orientation](
      Types.OTHER,
      (index: Index, value: Orientation, row: PrepareRow) => {
        val pgObj = new PGobject()
        pgObj.setType("orientation")
        pgObj.setValue(Show[Orientation].show(value))
        row.setObject(index, pgObj, Types.OTHER)
      }
    )

  given Decoder[Orientation] =
    decoder[Orientation] { row => index =>
      StringParser[Orientation].parse(row.getString(index)).get
    }

  given [A, B](using Encoder[A]): Encoder[Tagged[A, B]] =
    given MappedEncoding[Tagged[A, B], A] = MappedEncoding(_.untag)
    mappedEncoder[Tagged[A, B], A]

  given [A, B](using Decoder[A]): Decoder[Tagged[A, B]] =
    given MappedEncoding[A, Tagged[A, B]] = MappedEncoding(_.tag)
    mappedDecoder[A, Tagged[A, B]]
