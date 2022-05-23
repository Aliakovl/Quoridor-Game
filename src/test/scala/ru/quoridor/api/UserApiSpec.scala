package ru.quoridor.api

import cats.effect.IO
import org.reactormonk.{CryptoBits, PrivateKey}
import org.scalamock.clazz.MockImpl.mock
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.quoridor.api.UserApi
import ru.quoridor.services.UserService
import sttp.client3._
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.stub.TapirStubInterpreter

import scala.concurrent.Future
import scala.io.Codec
import scala.util.Random


class UserApiSpec extends AsyncFlatSpec  with Matchers{

//  private val mockUserService: UserService[IO] = mock[UserService[IO]]
//
//  private val key = PrivateKey(Codec.toUTF8(Random.alphanumeric.take(20).mkString("")))
//  val crypto = CryptoBits(key)
//
//  val userApi = new UserApi(mockUserService, crypto)
//
//  it should "wef" in {
//
//  }


}
