package model.services
import model.game.geometry.Board.initPosition
import model.game.geometry.Board
import model.{GameException, ProtoGame, User}
import model.game.{Game, GameState, Player}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class GameCreatorImpl(protoGameStorage: ProtoGameStorage,
                      gameStorage: GameStorage,
                      userStorage: UserStorage)(implicit ec: ExecutionContext) extends GameCreator {
  override def createGame(userId: UUID): Future[ProtoGame] = {
    protoGameStorage.insert(userId)
  }

  override def joinPlayer(gameId: UUID, userId: UUID): Future[ProtoGame] = {
    for {
      _ <- userStorage.find(userId)
      game <- protoGameStorage.update(gameId, userId)
    } yield game
  }

  override def startGame(gameId: UUID): Future[Game] = {
    for {
      protoGame <- protoGameStorage.find(gameId)
      users = protoGame.users
      players <- Future.fromTry(createPlayers(users).toTry)
      firstTurnPlayer = players.head
      state = GameState(players.toSet, Set.empty)
      game <- gameStorage.insert(protoGame.gameId, firstTurnPlayer.id, state)
    } yield game
  }


  private def createPlayers(users: Seq[User]): Either[GameException, Seq[Player]] = {
    for {
      order <- Board.playersOrder(users.size)
      players = users.zip(order).map{
        case (User(id), target) => Player(id, Board.initPosition(target.opposite), 21 / users.size, target)
      }
    } yield players
  }

}
