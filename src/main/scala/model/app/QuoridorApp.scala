package model.app

import cats.effect.{ExitCode, IO, IOApp}
import doobie.Transactor
import model.api.UserApi
import model.services.{UserService, UserServiceImpl}
import model.storage.UserStorage
import model.storage.sqlStorage.UserStorageImpl
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.ember.server.EmberServerBuilder
import sttp.tapir.server.http4s._
import org.http4s.implicits._


object QuoridorApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(QuridorGame.routes.orNotFound)
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)
}


object QuridorGame{

  implicit val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/",
    "postgres",
    "securepassword"
  )

  private val userStorage: UserStorage[IO] = new UserStorageImpl[IO]
  private val userService: UserService[IO] = new UserServiceImpl(userStorage)

  private val userApi = new UserApi(userService)

  val routes = Http4sServerInterpreter[IO]().toRoutes(userApi.api)
}