package ru.quoridor.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.Encoder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.http4s.{HttpRoutes, Request, Response, StaticFile}
import ru.quoridor.ExceptionResponse
import ru.quoridor.api.WSGameApi

import java.util.UUID


object QuoridorApp extends IOApp {
//  val openApi = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI[IO](
//    QuoridorGame.api,
//    "Quoridor game server",
//    "0.0.1"
//  )
//
//  val swagger = Http4sServerInterpreter[IO]().toRoutes(SwaggerUI[IO](openApi.toYaml))

  val assetsRoutes = resourceServiceBuilder[IO]("/assets").toRoutes

  val loginPage = HttpRoutes.of[IO] {
    case GET -> Root =>
      StaticFile.fromResource[IO]("/loginpage.html").getOrElseF(InternalServerError())
  }

  val crypto = QuoridorGame.crypto

  def uuidFromCookie(request: Request[IO]): Option[UUID] = {
    for {
      cookie <- request.cookies.find(_.name == "auth-cookie")
      uuid <- crypto.validateSignedToken(cookie.content)
    } yield UUID.fromString(uuid)
  }

  val cookieAuth = HttpRoutes.of[IO] {
    case request @ GET -> Root / UUIDVar(uuid) =>
      val confirmed = uuidFromCookie(request).contains(uuid)
      if (confirmed) {
        StaticFile.fromResource[IO]("/userpage.html").getOrElseF(InternalServerError())
      } else {
        IO(Response[IO](Unauthorized))
      }

    case request @ GET -> Root =>
      val uuidOpt = uuidFromCookie(request).map(_.toString)
      IO(Response[IO](MovedPermanently)
        .withHeaders(List(
          "Location" -> ("/" + uuidOpt.getOrElse("sign"))
        ))
      )
  }

  val gameCreationRoute = HttpRoutes.of[IO] {
    case GET -> Root / UUIDVar(_) =>
      StaticFile.fromResource[IO]("/gamecreationpage.html").getOrElseF(InternalServerError())
  }

  val gameSessionRoute = HttpRoutes.of[IO] {
    case GET -> Root / UUIDVar(_) =>
      StaticFile.fromResource[IO]("/gamesessionpage.html").getOrElseF(InternalServerError())
  }

  val httpApp: HttpRoutes[IO] = Router[IO](
    "assets" -> assetsRoutes,
    "/" -> cookieAuth,
    "sign" -> loginPage,
    "api" -> QuoridorGame.routes,
    "game-creation" -> gameCreationRoute,
    "game-session" -> gameSessionRoute
  )

  implicit val jsonEncode: Encoder[ExceptionResponse] = Encoder.forProduct1("errorMessage")(_.message)

  override def run(args: List[String]): IO[ExitCode] = BlazeServerBuilder[IO]
    .bindHttp(8080, "0.0.0.0")
    .withHttpWebSocketApp({ wsb =>
      Router[IO] (
        "/" -> httpApp,
        "ws" -> new WSGameApi(wsb, QuoridorGame.gameService).routeWs.handleError{ error =>
          Response(InternalServerError).withEntity(ExceptionResponse(error))
        }
      ).orNotFound
    })
    .serve
    .compile
    .drain
    .as(ExitCode.Success)
}