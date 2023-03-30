export type Game = {
    id: string,
    step: number,
    state: State,
    winner?: User
}

export type State = {
    players: Players,
    walls: WallPosition
}

export type WallPosition = {
    orientation: Orientation
    row: number
    column: number
}

export type Orientation = 'horizontal' | 'vertical'

export type Players = {
    activePlayer: Player,
    enemies: [Player]
}

export type Player = {
    id: string,
    username: string,
    pawnPosition: PawnPosition,
    wallsAmount: number,
    target: Side
}

export type PawnPosition = {
    row: number
    column: number
}

export type Side = 'north' | 'south' | 'east' | 'west'

export type User = {
    id: string,
    username: string
}


export type ProtoGame = {
    id: string,
    players: ProtoPlayers,
}

export type ProtoPlayers = {
    creator: ProtoPlayer,
    guests: [ProtoPlayer]
}

export type ProtoPlayer = {
    id: string,
    username: string,
    target: Side
}

export type GamePreView = {
    id: string,
    players: [User],
    winner?: User
}

export type Move = PawnMove | PlaceWall

export type PawnMove = {
    pawnPosition: PawnPosition
}

export type PlaceWall = {
    wallPosition: WallPosition
}
