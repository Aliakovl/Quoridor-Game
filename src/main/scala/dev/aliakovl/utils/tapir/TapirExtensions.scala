package dev.aliakovl.utils.tapir

import io.circe.syntax.given
import io.circe.*
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.{CodecFormat, StreamBodyIO, streamBinaryBody}
import zio.stream.{Stream, UStream, ZPipeline, ZStream}
import zio.*

trait TapirExtensions:
  def messageStreamBody[E: Encoder: Decoder, A: Encoder: Decoder]: StreamBodyIO[
    Stream[Throwable, Byte],
    Stream[E, A],
    ZioStreams
  ] =
    streamBinaryBody(ZioStreams)(CodecFormat.OctetStream())
      .map(
        _.viaFunction(parseBytes[E, A]).viaFunction(parseStreamMessage[E, A])
      )(
        _.viaFunction(serialiseToMessage[E, A])
          .viaFunction(ping)
          .viaFunction(serialiseToBytes[E, A])
      )

  private def parseBytes[E: Decoder, A: Decoder]
      : Stream[Throwable, Byte] => UStream[StreamMessage[E, A]] =
    stream => {
      stream
        .via(ZPipeline.utf8Decode)
        .mapZIO(str =>
          ZIO.fromEither(io.circe.parser.decode[StreamMessage[E, A]](str))
        )
        .orDie
    }

  private def parseStreamMessage[E, A]
      : UStream[StreamMessage[E, A]] => Stream[E, A] = stream => {
    stream.collectZIO {
      case StreamMessage.Message(message) => ZIO.succeed(message)
      case StreamMessage.Error(error)     => ZIO.fail(error)
    }
  }

  private def serialiseToBytes[E: Encoder, A: Encoder]
      : UStream[StreamMessage[E, A]] => UStream[Byte] =
    stream => {
      stream
        .map(_.asJson)
        .map(Printer.noSpaces.printToByteBuffer)
        .mapConcatChunk(b =>
          Chunk
            .fromByteBuffer(b)
            .appendedAll("\n\n".getBytes("UTF-8"))
        )
    }

  private def serialiseToMessage[E, A]
      : Stream[E, A] => UStream[StreamMessage[E, A]] = stream => {
    stream.either.map {
      case Right(value) => StreamMessage.Message(value)
      case Left(error)  => StreamMessage.Error(error)
    }
  }

  private def ping[E, A]: Stream[Nothing, StreamMessage[E, A]] => Stream[
    Nothing,
    StreamMessage[E, A]
  ] = stream => {
    stream.mergeHaltLeft(ZStream.tick(13.seconds).as(StreamMessage.Ping))
  }

  given Encoder[Nothing] = (_: Nothing) => Json.Null
  given Decoder[Nothing] = (_: HCursor) => null
