package model

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.Transactor
import doobie.implicits._
import doobie.postgres.implicits._
import model.game.geometry.Orientation.Horizontal
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

    val io = for {
      _ <- dropDB.transact(xa)
      _ <- createDB.transact(xa)
      user_1 <- userStorage.insert("Aleksei")
      user_2 <- userStorage.insert("Michal")
      user_3 <- userStorage.insert("Mike")
      user_4 <- userStorage.insert("Igor")
      pg <- gameCreator.createGame(user_1.id)
      _ <- gameCreator.joinPlayer(pg.gameId, user_2.id)
      _ <- gameCreator.joinPlayer(pg.gameId, user_3.id)
      _ <- gameCreator.joinPlayer(pg.gameId, user_4.id)
      game <- gameCreator.startGame(pg.gameId)
      _ = println(game)
      move1 = PawnMove(PawnPosition(7, 4))
      game1 <- gameService.makeMove(game.id, user_1.id, move1)
      _ = println(game1)
      move2 = PlaceWall(WallPosition(Horizontal, 6, 7))
      game2 <- gameService.makeMove(game1.id, user_4.id, move2)
    } yield game2

    println(io.unsafeRunSync())

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
          id UUID PRIMARY KEY
      );

      CREATE TABLE player (
          game_id UUID REFERENCES game,
          user_id UUID REFERENCES "user",
          target side NOT NULL,
          PRIMARY KEY (game_id, user_id)
      );

      CREATE TABLE game_state (
          id UUID PRIMARY KEY,
          game_id UUID REFERENCES game,
          previous_state UUID REFERENCES game_state,
          active_player UUID REFERENCES "user",
          FOREIGN KEY (game_id, active_player) REFERENCES player(game_id, user_id)
      );

      CREATE TABLE pawn_position (
          game_state_id UUID REFERENCES game_state,
          user_id UUID REFERENCES "user",
          wallsAmount INT NOT NULL,
          "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 8),
          "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 8),
          PRIMARY KEY (game_state_id, user_id)
      );

      CREATE TABLE wall_position (
          game_state_id UUID REFERENCES game_state,
          orient orientation NOT NULL,
          "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 8),
          "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 8)
      );
         """.update.run

  val dropDB =
    sql"""
      drop table pawn_position;

      drop table wall_position;

      drop table game_state;

      drop table player;

      drop table "user";

      drop table game;

      drop type side;

      drop type orientation;
         """.update.run
}
