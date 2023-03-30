import type {Game, Move} from "./types";

export class GameWS {
    private ws: WebSocket;

    constructor(gameId: string, token: string, getGame: (game: Game) => void) {
        this.ws = new WebSocket(`ws://localhost/ws/game/${gameId}?token=${token}`);
        this.ws.onmessage = message => {
            getGame(JSON.parse(message.data.toString()) as Game);
        }
    }

    send(move: Move) {
        this.ws.send(JSON.stringify(move));
    }
}