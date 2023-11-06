package ru.quoridor.auth.store

import io.lettuce.core.codec.{RedisCodec, StringCodec}
import io.circe.*
import io.circe.syntax.given
import ru.quoridor.auth.model.RefreshToken
import ru.quoridor.model.User
import ru.quoridor.model.game.Game
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged.*

import java.nio.ByteBuffer
import java.util.UUID

package object redis:
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

  given RedisCodec[ID[Game], Game] with
    override def decodeValue(bytes: ByteBuffer): Game =
      parser
        .decode(stringCodec.decodeKey(bytes))
        .getOrElse(throw new Throwable("qwef"))

    override def decodeKey(bytes: ByteBuffer): ID[Game] =
      UUID.fromString(stringCodec.decodeKey(bytes)).tag[Game]

    override def encodeValue(key: Game): ByteBuffer =
      Printer.noSpaces.printToByteBuffer(key.asJson)

    override def encodeKey(key: ID[Game]): ByteBuffer =
      stringCodec.encodeKey(key.toString)

    private val stringCodec: StringCodec = StringCodec.UTF8
