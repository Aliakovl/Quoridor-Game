package dev.aliakovl.quoridor

import dev.aliakovl.quoridor.api.data.Requests.*
import dev.aliakovl.quoridor.api.data.Responses.*
import dev.aliakovl.quoridor.auth.model.{ClaimData, Username}
import dev.aliakovl.quoridor.engine.Game
import dev.aliakovl.quoridor.model.User
import dev.aliakovl.quoridor.services.{GameCreator, GameService, UserService}
import dev.aliakovl.utils.tagging.ID
import zio.stream.ZStream
import zio.{RIO, Task, ZIO, ZLayer}

trait GameApiService:
  def createGame(claimData: ClaimData): Task[ProtoGameResponse]

  def joinPlayer(
      claimData: ClaimData
  )(gameId: ID[Game], userId: ID[User]): Task[ProtoGameResponse]

  def startGame(
      claimData: ClaimData
  )(gameId: ID[Game]): Task[GameResponse]

  def gameHistory(
      claimData: ClaimData
  )(gameId: ID[Game]): Task[List[GameResponse]]

  def history(claimData: ClaimData): Task[List[GamePreViewResponse]]

  def getGame(
      claimData: ClaimData
  )(gameId: ID[Game]): Task[GameResponse]

  def getUser(
      claimData: ClaimData
  )(username: Username): Task[UserResponse]

  def pawnMove(
      claimData: ClaimData
  )(gameId: ID[Game], move: PawnMoveRequest): Task[Unit]

  def placeWall(
      claimData: ClaimData
  )(gameId: ID[Game], move: PlaceWallRequest): Task[Unit]

  def availablePawnMoves(
      claimData: ClaimData
  )(gameId: ID[Game]): Task[List[PawnPositionResponse]]

  def availableWallMoves(
      claimData: ClaimData
  )(gameId: ID[Game]): Task[Set[WallPositionResponse]]

  def subscribeOnGame(
      claimData: ClaimData
  )(gameId: ID[Game]): Task[ZStream[Any, Throwable, GameResponse]]

object GameApiService:
  val live: ZLayer[
    GameCreator with GameService with UserService,
    Nothing,
    GameApiServiceImpl
  ] = ZLayer.fromFunction(new GameApiServiceImpl(_, _, _))

  def createGame(claimData: ClaimData): RIO[GameApiService, ProtoGameResponse] =
    ZIO.serviceWithZIO(_.createGame(claimData))

  def joinPlayer(
      claimData: ClaimData
  )(
      gameId: ID[Game],
      userId: ID[User]
  ): RIO[GameApiService, ProtoGameResponse] =
    ZIO.serviceWithZIO(_.joinPlayer(claimData)(gameId, userId))

  def startGame(
      claimData: ClaimData
  )(gameId: ID[Game]): RIO[GameApiService, GameResponse] =
    ZIO.serviceWithZIO(_.startGame(claimData)(gameId))

  def gameHistory(
      claimData: ClaimData
  )(gameId: ID[Game]): RIO[GameApiService, List[GameResponse]] =
    ZIO.serviceWithZIO(_.gameHistory(claimData)(gameId))

  def history(
      claimData: ClaimData
  ): RIO[GameApiService, List[GamePreViewResponse]] =
    ZIO.serviceWithZIO(_.history(claimData))

  def getGame(
      claimData: ClaimData
  )(gameId: ID[Game]): RIO[GameApiService, GameResponse] =
    ZIO.serviceWithZIO(_.getGame(claimData)(gameId))

  def getUser(
      claimData: ClaimData
  )(username: Username): RIO[GameApiService, UserResponse] =
    ZIO.serviceWithZIO(_.getUser(claimData)(username))

  def pawnMove(
      claimData: ClaimData
  )(gameId: ID[Game], move: PawnMoveRequest): RIO[GameApiService, Unit] =
    ZIO.serviceWithZIO(_.pawnMove(claimData)(gameId, move))

  def placeWall(
      claimData: ClaimData
  )(gameId: ID[Game], move: PlaceWallRequest): RIO[GameApiService, Unit] =
    ZIO.serviceWithZIO(_.placeWall(claimData)(gameId, move))

  def availablePawnMoves(
      claimData: ClaimData
  )(gameId: ID[Game]): RIO[GameApiService, List[PawnPositionResponse]] =
    ZIO.serviceWithZIO(_.availablePawnMoves(claimData)(gameId))

  def availableWallMoves(
      claimData: ClaimData
  )(gameId: ID[Game]): RIO[GameApiService, Set[WallPositionResponse]] =
    ZIO.serviceWithZIO(_.availableWallMoves(claimData)(gameId))

  def subscribeOnGame(claimData: ClaimData)(
      gameId: ID[Game]
  ): RIO[GameApiService, ZStream[Any, Throwable, GameResponse]] =
    ZIO.serviceWithZIO(_.subscribeOnGame(claimData)(gameId))

class GameApiServiceImpl(
    gameCreator: GameCreator,
    gameService: GameService,
    userService: UserService
) extends GameApiService:
  import scala.language.implicitConversions
  import dev.aliakovl.quoridor.api.data.Conversions.given

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
