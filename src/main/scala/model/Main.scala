package model

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.Transactor
import doobie.implicits._
import doobie.postgres.implicits._
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import model.game.geometry.Orientation.{Horizontal, Vertical}
import model.game.{PawnMove, PlaceWall}
import model.game.geometry.{PawnPosition, WallPosition}
import model.services.{GameCreator, GameCreatorImpl, GameService, GameServiceImpl}
import model.storage.sqlStorage.{GameStorageImpl, ProtoGameStorageImpl, UserStorageImpl}
import model.storage.{GameStorage, ProtoGameStorage, UserStorage}


object Main {
  def main(args: Array[String]): Unit = {

    implicit val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/",
      "postgres",
      "securepassword"
    )

    val protoGameStorage: ProtoGameStorage[IO] = new ProtoGameStorageImpl[IO]
    val gameStorage: GameStorage[IO] = new GameStorageImpl[IO]
    val userStorage: UserStorage[IO] = new UserStorageImpl[IO]

    val gameCreator: GameCreator[IO] = new GameCreatorImpl(protoGameStorage, gameStorage, userStorage)
    val gameService: GameService[IO] = new GameServiceImpl(protoGameStorage, gameStorage, userStorage)

    val newGame = for {
      _ <- dropDB.transact(xa)
      _ <- createDB.transact(xa)
      user_1 <- userStorage.insert("Aleksei")
      user_2 <- userStorage.insert("May")
      user_3 <- userStorage.insert("August")
      user_4 <- userStorage.insert("Mart")
      pg <- gameCreator.createGame(user_1.id)
      _ <- gameCreator.joinPlayer(pg.id, user_2.id)
      _ <- gameCreator.joinPlayer(pg.id, user_3.id)
      _ <- gameCreator.joinPlayer(pg.id, user_4.id)
      game <- gameCreator.startGame(pg.id, user_1.id)
    } yield (game.id, user_1.id, user_2.id, user_3.id, user_4.id)


    println(newGame.flatMap{ case (id, id1, id2, id3, id4) =>
      gameService.makeMove(id, id1, PawnMove(PawnPosition(7, 4)))
        .flatMap(g => gameService.makeMove(g.id, id4, PlaceWall(WallPosition(Vertical, 1, 4))))
        .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(1, 4))))
        .flatMap(g => gameService.makeMove(g.id, id3, PlaceWall(WallPosition(Vertical, 6, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(6, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(2, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(5, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(3, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(4, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(5, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(3, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(6, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(2, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(7, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(1, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id2, PawnMove(PawnPosition(8, 4))))
//      .flatMap(g => gameService.makeMove(g.id, id1, PawnMove(PawnPosition(0, 4))))
    }.unsafeRunSync()
    )

    println(PawnMove(PawnPosition(1, 4)).asJson)

  }

  val createDB =
    sql"""
      CREATE TYPE side as ENUM (
          'north',
          'south',
          'west',
          'east'
      );

      CREATE TYPE orientation as ENUM (
          'horizontal',
          'vertical'
      );

      CREATE TABLE "user" (
          id UUID PRIMARY KEY,
          login varchar(32) NOT NULL UNIQUE
      );

      CREATE TABLE game (
          id UUID PRIMARY KEY,
          creator UUID NOT NULL REFERENCES "user"
      );

      CREATE TABLE player (
          game_id UUID NOT NULL REFERENCES game,
          user_id UUID NOT NULL REFERENCES "user",
          target side NOT NULL,
          PRIMARY KEY (game_id, user_id),
          UNIQUE (game_id, target)
      );

      CREATE TABLE game_state (
          id UUID PRIMARY KEY,
          game_id UUID NOT NULL,
          previous_state UUID NOT NULL REFERENCES game_state,
          active_player UUID NOT NULL,
          winner UUID,
          FOREIGN KEY (game_id, active_player) REFERENCES player(game_id, user_id),
          FOREIGN KEY (game_id, winner) REFERENCES player(game_id, user_id)
      );

      CREATE TABLE pawn_position (
          game_state_id UUID REFERENCES game_state,
          user_id UUID NOT NULL REFERENCES "user",
          walls_amount INT NOT NULL,
          "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 8),
          "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 8),
          PRIMARY KEY (game_state_id, user_id)
      );

      CREATE TABLE wall_position (
          game_state_id UUID NOT NULL REFERENCES game_state,
          orient orientation NOT NULL,
          "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 7),
          "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 7)
      );
         """.update.run

  val dropDB =
    sql"""
      drop table pawn_position;

      drop table wall_position;

      drop table game_state;

      drop table player;

      drop table game;

      drop table "user";

      drop type side;

      drop type orientation;
         """.update.run
}
