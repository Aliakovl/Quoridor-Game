import type {Game, Move} from "./types";
import {dev} from "$app/environment";

export class GameWS {
    private ws: WebSocket;

    constructor(gameId: string, token: string, getGame: (game: Game) => void) {
        const ws = dev ? "ws" : "wss";
        this.ws = new WebSocket(`${ws}://${location.host}/ws/game/${gameId}?token=${token}`);
        this.ws.onmessage = message => {
            getGame(JSON.parse(message.data.toString()) as Game);
        }
    }

    send(move: Move) {
        this.ws.send(JSON.stringify(move));
    }
}