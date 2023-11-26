package dev.aliakovl.quoridor.api.data

import dev.aliakovl.quoridor.api.data.Requests.{
  PawnMoveRequest,
  PawnPositionRequest,
  PlaceWallRequest,
  WallPositionRequest
}
import dev.aliakovl.quoridor.api.data.Responses.*
import dev.aliakovl.quoridor.engine.{Game, Move, Player, Players, State}
import dev.aliakovl.quoridor.engine.geometry.{PawnPosition, WallPosition}
import dev.aliakovl.quoridor.model.{
  GamePreView,
  ProtoGame,
  ProtoPlayer,
  ProtoPlayers,
  User
}

import scala.language.implicitConversions

object Conversions:
  given Conversion[User, UserResponse] = { case User(id, username) =>
    UserResponse(id, username)
  }

  given Conversion[WallPosition, WallPositionResponse] = {
    case WallPosition(orientation, row, column) =>
      WallPositionResponse(orientation, row, column)
  }

  given Conversion[PawnPosition, PawnPositionResponse] = {
    case PawnPosition(row, column) => PawnPositionResponse(row, column)
  }

  given Conversion[Player, PlayerResponse] = {
    case Player(id, username, pawnPosition, wallsAmount, target) =>
      PlayerResponse(id, username, pawnPosition, wallsAmount, target)
  }

  given Conversion[Players, PlayersResponse] = {
    case Players(activePlayer, enemies) =>
      PlayersResponse(activePlayer, enemies.map(_.convert))
  }

  given Conversion[State, StateResponse] = { case State(players, walls) =>
    StateResponse(players, walls.map(_.convert))
  }

  given Conversion[Game, GameResponse] = { case Game(id, step, state, winner) =>
    GameResponse(id, step, state, winner.map(_.convert))
  }

  given Conversion[ProtoGame, ProtoGameResponse] = {
    case ProtoGame(id, players) => ProtoGameResponse(id, players)
  }

  given Conversion[ProtoPlayer, ProtoPlayerResponse] = {
    case ProtoPlayer(id, username, target) =>
      ProtoPlayerResponse(id, username, target)
  }

  given Conversion[ProtoPlayers, ProtoPlayersResponse] = {
    case ProtoPlayers(creator, guests) =>
      ProtoPlayersResponse(creator, guests.map(_.convert))
  }

  given Conversion[GamePreView, GamePreViewResponse] = {
    case GamePreView(id, players, winner) =>
      GamePreViewResponse(id, players.map(_.convert), winner.map(_.convert))
  }

  given Conversion[PawnPositionRequest, PawnPosition] = {
    case PawnPositionRequest(row, column) => PawnPosition(row, column)
  }

  given Conversion[WallPositionRequest, WallPosition] = {
    case WallPositionRequest(orientation, row, column) =>
      WallPosition(orientation, row, column)
  }

  given Conversion[PawnMoveRequest, Move.PawnMove] = {
    case PawnMoveRequest(pawnPosition) => Move.PawnMove(pawnPosition)
  }

  given Conversion[PlaceWallRequest, Move.PlaceWall] = {
    case PlaceWallRequest(wallPosition) => Move.PlaceWall(wallPosition)
  }
