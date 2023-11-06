import {refresh} from "../auth/authAPI";
import {getToken, saveToken} from "../auth/auth";
import type {Game, GamePreView, Move, PawnMove, PawnPosition, PlaceWall, ProtoGame, User, WallPosition} from "./types";
import {browser} from "$app/environment";

export function directLogin() {
    if (browser) {
        window.location.replace('/login');
    }
}

export class GameAPI {
    private accessToken: string;

    constructor(accessToken: string) {
        this.accessToken = accessToken;
    }

    static Unsafe() {
        const token = getToken();
        if (token == undefined) {
            directLogin();
        } else {
            return new GameAPI(token);
        }
    }

    updateToken(accessToken: string) {
        this.accessToken = accessToken;
    }

    async wrapper(request: () => Promise<Response>) {
        const response = await request();
        if (response.status === 401) {
            const accessToken = await refresh();
            this.updateToken(accessToken);
            saveToken(accessToken);
            return await request();
        }
        return response;
    }

    call(url: RequestInfo | URL, method?: string, body?: BodyInit) {
        return this.wrapper(() => {
            return fetch(url, {
                method: method,
                headers: {
                    "Authorization": `Bearer ${this.accessToken}`
                },
                body: body
            })
        });
    }

    async createGame() {
        const response = await this.call('/api/v1/game/create', 'POST');
        if (!response.ok) {
            throw new Error("createGame failed");
        }
        return await response.json() as ProtoGame;
    }

    async joinPlayer(gameId: string, userId: string) {
        const response = await this.call(`/api/v1/game/${gameId}/join/${userId}`, 'POST');
        if (!response.ok) {
            throw new Error("joinPlayer failed");
        }
        return await response.json() as ProtoGame;
    }

    async startGame(gameId: string) {
        const response = await this.call(`/api/v1/game/${gameId}/start`, 'POST');
        if (!response.ok) {
            throw new Error("startGame failed");
        }
        return await response.json() as Game;
    }

    async gameHistory(gameId: string) {
        const response = await this.call(`/api/v1/game/${gameId}/history`);
        if (!response.ok) {
            throw new Error("gameHistory failed");
        }
        return await response.json() as [Game];
    }

    async history() {
        const response = await this.call('/api/v1/history');
        if (!response.ok) {
            throw new Error("history failed");
        }
        return await response.json() as [GamePreView];
    }

    async getGame(gameId: string) {
        const response = await this.call(`/api/v1/game/${gameId}`);
        if (!response.ok) {
            throw new Error("getGame failed");
        }
        return await response.json() as Game;
    }

    async subGame(gameId: string, token: string, onMessage: (game: Game) => void) {
        const response = await this.call(`/stream/api/v1/game/${gameId}`, 'GET');
        const writable = new WritableStream({
            write(chunk) {
                const game: Game = JSON.parse(chunk);
                onMessage(game);
            }
        });
        await response.body
            .pipeThrough(new TextDecoderStream("utf-8"))
            .pipeTo(writable);
    }

    async getUser(username: string) {
        const response = await this.call(`/api/v1/user/${username}`);
        if (!response.ok) {
            throw new Error("getGame failed");
        }
        return await response.json() as User;
    }

    async pawnMove(gameId: string, move: PawnMove) {
        const response = await this.call(`/api/v1/game/${gameId}/movePawn`, 'POST', JSON.stringify(move));
        if (!response.ok) {
            throw new Error("pawnMove failed");
        }
    }

    async placeWall(gameId: string, move: PlaceWall) {
        const response = await this.call(`/api/v1/game/${gameId}/placeWall`, 'POST', JSON.stringify(move));
        if (!response.ok) {
            throw new Error("placeWall failed");
        }
    }

    async pawnMoves(gameId: string) {
        const response = await this.call(`/api/v1/game/${gameId}/pawnMoves`);
        if (!response.ok) {
            throw new Error("pawnMoves failed");
        }
        return await response.json() as [PawnPosition];
    }

    async wallMoves(gameId: string) {
        const response = await this.call(`/api/v1/game/${gameId}/wallMoves`);
        if (!response.ok) {
            throw new Error("wallMoves failed");
        }
        return await response.json() as [WallPosition];
    }
}
