package model

import model.game.geometry.Orientation.Horizontal
import model.game.{PawnMove, PlaceWall}
import model.game.geometry.{PawnPosition, WallPosition}
import model.services.{GameCreator, GameCreatorImpl, GameService, GameServiceImpl}
import model.storage.InMemoryStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object Main {
  def main(args: Array[String]): Unit = {
    val protoGameStorage: ProtoGameStorage = new ProtoGameStorageImpl
    val gameStorage: GameStorage = new GameStorageImpl
    val userStorage: UserStorage = new UserStorageImpl

    val gameCreator: GameCreator = new GameCreatorImpl(protoGameStorage, gameStorage, userStorage)
    val gameService: GameService = new GameServiceImpl(protoGameStorage, gameStorage, userStorage)

    val future = for {
      user_1 <- userStorage.insert
      user_2 <- userStorage.insert
      pg <- gameCreator.createGame(user_1.id)
      pgn <- gameCreator.joinPlayer(pg.gameId, user_2.id)
      game <- gameCreator.startGame(pgn.gameId)
      _ = println(game)
      move1 = PawnMove(PawnPosition(7, 4))
      game1 <- gameService.makeMove(game.id, user_2.id, move1)
      _ = println(game1)
      move2 = PlaceWall(WallPosition(Horizontal, 6, 3))
      game2 <- gameService.makeMove(game1.id, user_1.id, move2)
    } yield game2

    println(Await.result(future, 1.second))

  }
}
