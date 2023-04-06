package ru.quoridor.api

import sttp.tapir.ztapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import ru.quoridor.auth.AuthorizationService
import ru.quoridor.auth.AuthorizationService.validate
import ru.quoridor.auth.model.{AccessToken, Username}
import ru.quoridor.model.game.geometry.PawnPosition
import ru.quoridor.model.{GamePreView, ProtoGame, User}
import ru.quoridor.model.game.{Game, Move}
import ru.quoridor.services.GameCreator._
import ru.quoridor.services.{GameCreator, GameService, UserService}
import ru.quoridor.services.GameService._
import ru.quoridor.services.UserService.getUser
import ru.utils.tagging.ID
import ru.utils.tagging.Tagged._
import sttp.model.StatusCode

object GameAPI {
  def apply[
      Env <: GameService with GameCreator with UserService with AuthorizationService
  ]: List[ZServerEndpoint[Env, Any]] = List(
    createGameEndpoint.widen[Env],
    joinPlayerEndpoint.widen[Env],
    startGameEndpoint.widen[Env],
    gameHistoryEndpoint.widen[Env],
    historyEndpoint.widen[Env],
    getGameEndpoint.widen[Env],
    getUserEndpoint.widen[Env],
    moveEndpoint.widen[Env],
    pawnMoves.widen[Env]
  )

  private val baseEndpoint =
    endpoint
      .in("api")
      .securityIn(auth.bearer[AccessToken]())
      .errorOut(jsonBody[ExceptionResponse] and statusCode)
      .mapErrorOut(er => new Throwable(er._1.errorMessage))(
        ExceptionResponse(_)
      )
      .zServerSecurityLogic { accessToken =>
        validate(accessToken)
      }

  private val createGameEndpoint =
    baseEndpoint.post
      .in("game" / "create")
      .out(jsonBody[ProtoGame] and statusCode(StatusCode.Created))
      .serverLogic { claimData => _ =>
        createGame(claimData.userId)
      }

  private val joinPlayerEndpoint =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId"))
      .in("join" / path[ID[User]]("userId"))
      .out(jsonBody[ProtoGame])
      .serverLogic { _ =>
        { case (gameId, userId) =>
          joinPlayer(gameId, userId)
        }
      }

  private val startGameEndpoint =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "start")
      .out(jsonBody[Game] and statusCode(StatusCode.Created))
      .serverLogic { claimData => gameId =>
        startGame(gameId, claimData.userId)
      }

  private val gameHistoryEndpoint =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId") / "history")
      .out(jsonBody[List[Game]])
      .serverLogic { claimData => gameId =>
        gameHistory(gameId, claimData.userId)
      }

  private val historyEndpoint =
    baseEndpoint.get
      .in("history")
      .out(jsonBody[List[GamePreView]])
      .serverLogic { claimData => _ =>
        usersHistory(claimData.userId)
      }

  private val getGameEndpoint =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId"))
      .out(jsonBody[Game])
      .serverLogic { _ => gameId =>
        findGame(gameId)
      }

  private val getUserEndpoint =
    baseEndpoint.get
      .in("user" / path[Username]("username"))
      .out(jsonBody[User])
      .serverLogic { _ => username =>
        getUser(username)
      }

  private val moveEndpoint =
    baseEndpoint.post
      .in("game" / path[ID[Game]]("gameId") / "move")
      .in(jsonBody[Move])
      .out(jsonBody[Game])
      .serverLogic { claimData =>
        { case (gameId, move) =>
          makeMove(gameId, claimData.userId, move)
        }
      }

  private val pawnMoves =
    baseEndpoint.get
      .in("game" / path[ID[Game]]("gameId") / "pawnMoves")
      .out(jsonBody[List[PawnPosition]])
      .serverLogic { claimData => gameId =>
        availablePawnMoves(gameId, claimData.userId)
      }
}
