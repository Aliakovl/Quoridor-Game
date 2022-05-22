package model.app

import cats.effect.IO
import doobie.Transactor
import model.api.{GameApi, UserApi}
import model.services.{GameCreator, GameCreatorImpl, GameService, GameServiceImpl, UserService, UserServiceImpl}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}
import model.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}
import org.reactormonk.{CryptoBits, PrivateKey}
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.io.Codec
import scala.util.Random

object QuoridorGame {

  implicit val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://db:5432/",
    "postgres",
    "postgres"
  )

  private val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO]
  private val gameStorage: GameStorage[IO] = new GameStorageImpl[IO]

  private val userStorage: UserStorage[IO] = new UserStorageImpl[IO]
  val userService: UserService[IO] = new UserServiceImpl(userStorage, gameStorage)

  private val gameCreator: GameCreator[IO] = new GameCreatorImpl[IO](
    protoGameStorage, gameStorage, userStorage
  )

  val gameService: GameService[IO] = new GameServiceImpl[IO](
    protoGameStorage, gameStorage, userStorage
  )

  private val key = PrivateKey(Codec.toUTF8(Random.alphanumeric.take(20).mkString("")))
  val crypto = CryptoBits(key)

  private val userApi = new UserApi(userService, crypto)
  private val gameApi = new GameApi(userService, gameCreator, gameService)

  val api =  userApi.api ::: gameApi.api

  val routes = Http4sServerInterpreter[IO]().toRoutes(api)
}