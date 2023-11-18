package ru.utils

import io.circe.{Decoder, Encoder, Printer}
import io.circe.syntax.given
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.{CodecFormat, StreamBodyIO, streamBinaryBody}
import zio.{Chunk, ZIO}
import zio.stream.{Stream, ZPipeline}

trait TapirExtensions:
  def zioStreamBody[T: Encoder: Decoder]: StreamBodyIO[
    Stream[Throwable, Byte],
    Stream[Throwable, T],
    ZioStreams
  ] =
    streamBinaryBody(ZioStreams)(CodecFormat.OctetStream())
      .map(parseBytes)(serialiseToBytes)

  private def parseBytes[T: Decoder]
      : Stream[Throwable, Byte] => Stream[Throwable, T] =
    stream => {
      stream
        .via(ZPipeline.utf8Decode)
        .mapZIO(str => ZIO.fromEither(io.circe.parser.decode(str)))
        .orDie
    }

  private def serialiseToBytes[T: Encoder]
      : Stream[Throwable, T] => Stream[Throwable, Byte] = stream => {
    stream
      .map(_.asJson)
      .map(Printer.noSpaces.printToByteBuffer)
      .mapConcatChunk(Chunk.fromByteBuffer)
  }
