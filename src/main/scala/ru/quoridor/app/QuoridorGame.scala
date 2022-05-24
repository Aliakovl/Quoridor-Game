package ru.quoridor.app

import cats.effect.IO
import cats.effect.kernel.Resource
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig._
import pureconfig.generic.auto._
import ru.quoridor.api.{GameApi, UserApi}
import ru.quoridor.services.{GameCreator, GameCreatorImpl, GameService, GameServiceImpl, UserService, UserServiceImpl}
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.io.Codec
import scala.util.Random

object QuoridorGame {

  val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

  private val resourceTransactor: Resource[IO, HikariTransactor[IO]] = for {
    ce <- ExecutionContexts.fixedThreadPool[IO](32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      appConfig.DB.driver,
      appConfig.DB.url,
      appConfig.DB.user,
      appConfig.DB.password,
      ce
    )
  } yield xa

  private val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO](resourceTransactor)
  private val gameStorage: GameStorage[IO] = new GameStorageImpl[IO](resourceTransactor)
  private val userStorage: UserStorage[IO] = new UserStorageImpl[IO](resourceTransactor)

  val userService: UserService[IO] = new UserServiceImpl(userStorage, gameStorage)
  val gameCreator: GameCreator[IO] = new GameCreatorImpl[IO](protoGameStorage, gameStorage)
  val gameService: GameService[IO] = new GameServiceImpl[IO](gameStorage)

  private val key = PrivateKey(Codec.toUTF8(Random.alphanumeric.take(20).mkString("")))
  val crypto = CryptoBits(key)

  private val userApi = new UserApi(userService, crypto)
  private val gameApi = new GameApi(userService, gameCreator, gameService)

  val api =  userApi.api ::: gameApi.api

  val apiRoutes = Http4sServerInterpreter[IO]().toRoutes(api)
}