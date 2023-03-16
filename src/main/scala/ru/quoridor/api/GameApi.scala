package ru.quoridor.api

import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.auth.AuthorizationService
import ru.quoridor.auth.AuthorizationService.validate
import ru.quoridor.auth.model.AccessToken
import ru.quoridor.model.{GamePreView, ProtoGame, User}
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.services.{GameCreator, GameService}
import ru.quoridor.services.GameCreator._
import ru.quoridor.services.GameService._
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged._
import sttp.model.StatusCode

object GameApi {

  private val baseEndpoint =
    endpoint
      .in("api")
      .securityIn(auth.bearer[AccessToken]())
      .errorOut(jsonBody[ExceptionResponse] and statusCode)
      .mapErrorOut(er => new Throwable(er._1.errorMessage)) {
        ExceptionResponse(_)
      }
      .zServerSecurityLogic { accessToken =>
        validate(accessToken)
      }

  val createGameEndpoint
      : ZServerEndpoint[AuthorizationService with GameCreator, Any] =
    baseEndpoint.post
      .in("game" / "create")
      .out(jsonBody[ProtoGame] and statusCode(StatusCode.Created))
      .serverLogic { claimData => _ =>
        createGame(claimData.userId)
      }

  val joinPlayerEndpoint
      : ZServerEndpoint[AuthorizationService with GameCreator, Any] =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId"))
      .in("join" / path[ID[User]]("userId"))
      .out(jsonBody[ProtoGame])
      .serverLogic { _ =>
        { case (gameId, userId) =>
          joinPlayer(gameId, userId)
        }
      }

  val startGameEndpoint
      : ZServerEndpoint[AuthorizationService with GameCreator, Any] =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "start")
      .out(jsonBody[Game] and statusCode(StatusCode.Created))
      .serverLogic { claimData => gameId =>
        startGame(gameId, claimData.userId)
      }

  val gameHistoryEndpoint
      : ZServerEndpoint[AuthorizationService with GameService, Any] =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId") / "history")
      .out(jsonBody[List[Game]])
      .serverLogic { claimData => gameId =>
        gameHistory(gameId, claimData.userId)
      }

  val historyEndpoint
      : ZServerEndpoint[AuthorizationService with GameService, Any] =
    baseEndpoint.get
      .in("history")
      .out(jsonBody[List[GamePreView]])
      .serverLogic { claimData => _ =>
        usersHistory(claimData.userId)
      }

  val getGameEndpoint
      : ZServerEndpoint[AuthorizationService with GameService, Any] =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId"))
      .out(jsonBody[Game])
      .serverLogic { _ => gameId =>
        findGame(gameId)
      }

  val moveEndpoint
      : ZServerEndpoint[AuthorizationService with GameService, Any] =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "move")
      .in(jsonBody[Move])
      .out(jsonBody[Game])
      .serverLogic { claimData =>
        { case (gameId, move) =>
          makeMove(gameId, claimData.userId, move)
        }
      }
}
