package dev.aliakovl.quoridor.api

import dev.aliakovl.quoridor.api.data.Requests.*
import dev.aliakovl.quoridor.api.data.Responses.*
import dev.aliakovl.quoridor.auth.model.{ClaimData, Username}
import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.quoridor.services.{GameCreator, GameService, UserService}
import dev.aliakovl.utils.tagging.ID
import zio.Task
import zio.stream.ZStream

class GameApiServiceImpl(
    gameCreator: GameCreator,
    gameService: GameService,
    userService: UserService
) extends GameApiService:
  import dev.aliakovl.quoridor.api.data.Conversions.given

  import scala.language.implicitConversions

  override def createGame(claimData: ClaimData): Task[ProtoGameResponse] =
    gameCreator.createGame(claimData.userId).map(_.convert)

  override def joinPlayer(
      claimData: ClaimData
  )(gameId: ID[Game], userId: ID[User]): Task[ProtoGameResponse] =
    gameCreator.joinPlayer(gameId, userId).map(_.convert)

  override def startGame(claimData: ClaimData)(
      gameId: ID[Game]
  ): Task[GameResponse] =
    gameCreator.startGame(gameId, claimData.userId).map(_.convert)

  override def gameHistory(claimData: ClaimData)(
      gameId: ID[Game]
  ): Task[List[GameResponse]] =
    gameService.gameHistory(gameId, claimData.userId).map(_.map(_.convert))

  override def history(claimData: ClaimData): Task[List[GamePreViewResponse]] =
    gameService.usersHistory(claimData.userId).map(_.map(_.convert))

  override def getGame(claimData: ClaimData)(
      gameId: ID[Game]
  ): Task[GameResponse] = gameService.findGame(gameId).map(_.convert)

  override def getUser(claimData: ClaimData)(
      username: Username
  ): Task[UserResponse] = userService.getUser(username).map(_.convert)

  override def pawnMove(
      claimData: ClaimData
  )(gameId: ID[Game], move: PawnMoveRequest): Task[Unit] =
    gameService.makeMove(gameId, claimData.userId, move).map(_.convert)

  override def placeWall(
      claimData: ClaimData
  )(gameId: ID[Game], move: PlaceWallRequest): Task[Unit] =
    gameService.makeMove(gameId, claimData.userId, move).map(_.convert)

  override def availablePawnMoves(claimData: ClaimData)(
      gameId: ID[Game]
  ): Task[List[PawnPositionResponse]] =
    gameService
      .availablePawnMoves(gameId, claimData.userId)
      .map(_.map(_.convert))

  override def availableWallMoves(claimData: ClaimData)(
      gameId: ID[Game]
  ): Task[Set[WallPositionResponse]] =
    gameService.availableWallMoves(gameId).map(_.map(_.convert))

  override def subscribeOnGame(claimData: ClaimData)(
      gameId: ID[Game]
  ): Task[ZStream[Any, Throwable, GameResponse]] =
    gameService.subscribeOnGame(gameId).map(_.map(_.convert))
