package ru.quoridor.app

import cats.effect.kernel.Resource
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import io.circe.generic.auto._
import org.reactormonk.{CryptoBits, PrivateKey}
import pureconfig._
import pureconfig.generic.auto._
import ru.quoridor.ExceptionResponse
import ru.quoridor.api.{GameApi, UserApi}
import ru.quoridor.services._
import ru.quoridor.storage.{GameStorage, ProtoGameStorage, UserStorage}
import ru.quoridor.storage.sqlStorage._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.statusCode
import zio.Task
import zio.interop.catz._

import scala.io.Codec
import scala.util.Random

object QuoridorGame {

  val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

  private val resourceTransactor: Resource[Task, HikariTransactor[Task]] = for {
    ce <- ExecutionContexts.fixedThreadPool[Task](32)
    xa <- HikariTransactor.newHikariTransactor[Task](
      appConfig.DB.driver,
      appConfig.DB.url,
      appConfig.DB.user,
      appConfig.DB.password,
      ce
    )
  } yield xa

  private val protoGameStorage: ProtoGameStorage = new ProtoGameStorageImpl(
    resourceTransactor
  )
  private val gameStorage: GameStorage = new GameStorageImpl(resourceTransactor)
  private val userStorage: UserStorage = new UserStorageImpl(resourceTransactor)

  val userService: UserService = new UserServiceImpl(userStorage, gameStorage)
  val gameCreator: GameCreator =
    new GameCreatorImpl(protoGameStorage, gameStorage)
  val gameService: GameService = new GameServiceImpl(gameStorage)

  private val key = PrivateKey(
    Codec.toUTF8(Random.alphanumeric.take(20).mkString(""))
  )
  val crypto = CryptoBits(key)

  private val userApi = new UserApi(userService, crypto)
  private val gameApi = new GameApi(userService, gameCreator, gameService)

  val api = userApi.api ::: gameApi.api

  val serverOptions = Http4sServerOptions
    .customiseInterceptors[Task]
    .exceptionHandler(ExceptionHandler.pure[Task] { ctx =>
      Some(
        ValuedEndpointOutput(
          jsonBody[ExceptionResponse],
          ExceptionResponse(ctx.e)
        ).prepend(statusCode, ExceptionResponse.exceptionCode(ctx.e))
      )
    })
    .options

  val apiRoutes = ZHttp4sServerInterpreter(serverOptions).from(api).toRoutes
}
