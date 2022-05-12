package model.app

import cats.effect.{ExitCode, IO, IOApp}
import doobie.Transactor
import model.api._
import model.services._
import model.storage._
import model.storage.sqlStorage._
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.ember.server.EmberServerBuilder
import sttp.tapir.server.http4s._
import org.http4s.implicits._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.swagger.SwaggerUI


object QuoridorApp extends IOApp {
  val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI[IO](
    QuridorGame.api,
    "Quoridor game server",
    "0.0.1"
  )

  val swagger = Http4sServerInterpreter[IO]().toRoutes(SwaggerUI[IO](openApi.toYaml))


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

  private val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO]
  private val gameStorage: GameStorage[IO] = new GameStorageImpl[IO]

  private val gameCreator: GameCreator[IO] = new GameCreatorImpl[IO](
    protoGameStorage, gameStorage, userStorage
  )

  private val gameService: GameService[IO] = new GameServiceImpl[IO](
    protoGameStorage, gameStorage, userStorage
  )

  private val userApi = new UserApi(userService)
  private val gameApi = new GameApi(userService, gameCreator, gameService)

  val api =  userApi.api ::: gameApi.api

  val routes = Http4sServerInterpreter[IO]().toRoutes(api)
}