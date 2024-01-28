package dev.aliakovl.quoridor.codec

import dev.aliakovl.quoridor.auth.model.RefreshToken
import dev.aliakovl.quoridor.model.{Game, User}
import dev.aliakovl.quoridor.services.model.GameResponse
import dev.aliakovl.utils.tagging.ID
import dev.aliakovl.utils.tagging.Tagged.*
import io.lettuce.core.codec.{RedisCodec, StringCodec}
import io.circe.*
import io.circe.syntax.given

import java.nio.ByteBuffer
import java.util.UUID

object redis:
  given RedisCodec[RefreshToken, ID[User]] with
    override def decodeValue(bytes: ByteBuffer): ID[User] =
      UUID.fromString(stringCodec.decodeKey(bytes)).tag[User]

    override def decodeKey(bytes: ByteBuffer): RefreshToken =
      RefreshToken(stringCodec.decodeKey(bytes))

    override def encodeValue(key: ID[User]): ByteBuffer =
      stringCodec.encodeKey(key.toString)

    override def encodeKey(value: RefreshToken): ByteBuffer =
      stringCodec.encodeKey(value.value)

    private val stringCodec: StringCodec = StringCodec.UTF8

  given RedisCodec[ID[Game], GameResponse] with
    override def decodeValue(bytes: ByteBuffer): GameResponse =
      parser.decode[GameResponse](stringCodec.decodeKey(bytes)) match
        case Right(value) => value
        case Left(error)  => throw error.fillInStackTrace()

    override def decodeKey(bytes: ByteBuffer): ID[Game] =
      UUID.fromString(stringCodec.decodeKey(bytes)).tag[Game]

    override def encodeValue(key: GameResponse): ByteBuffer =
      Printer.noSpaces.printToByteBuffer(key.asJson)

    override def encodeKey(key: ID[Game]): ByteBuffer =
      stringCodec.encodeKey(key.toString)

    private val stringCodec: StringCodec = StringCodec.UTF8
