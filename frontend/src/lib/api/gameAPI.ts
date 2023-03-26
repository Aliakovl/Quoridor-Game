import {refresh} from "../auth/authAPI";
import {getToken, saveToken} from "../auth/auth";
import type {Game, GamePreView, Move, ProtoGame, User} from "./types";
import {browser} from "$app/environment";

export function directLogin() {
    if (browser) {
        window.location.replace('/login')
    }
}

export class GameAPI {
    private accessToken: string

    constructor(accessToken: string) {
        this.accessToken = accessToken
    }

    static Unsafe() {
        const token = getToken()
        if (token == undefined) {
            directLogin()
        } else {
            return new GameAPI(token)
        }
    }

    updateToken(accessToken: string) {
        this.accessToken = accessToken
    }

    async wrapper(request: () => Promise<Response>) {
        const response = await request()
        if (response.status === 401) {
            const accessToken = await refresh()
            this.updateToken(accessToken)
            saveToken(accessToken)
            return await request()
        }
        return response
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
        })
    }

    async createGame() {
        const response = await this.call('/api/game/create', 'POST')
        if (!response.ok) {
            throw new Error("createGame failed")
        }
        return await response.json() as ProtoGame
    }

    async joinPlayer(gameId: string, userId: string) {
        const response = await this.call(`/api/game/${gameId}/join/${userId}`, 'POST')
        if (!response.ok) {
            throw new Error("joinPlayer failed")
        }
        return await response.json() as ProtoGame
    }

    async startGame(gameId: string) {
        const response = await this.call(`/api/game/${gameId}/start`, 'POST')
        if (!response.ok) {
            throw new Error("startGame failed")
        }
        return await response.json() as Game
    }

    async gameHistory(gameId: string) {
        const response = await this.call(`/api/game/${gameId}/history`)
        if (!response.ok) {
            throw new Error("gameHistory failed")
        }
        return await response.json() as [Game]
    }

    async history() {
        const response = await this.call('/api/history')
        if (!response.ok) {
            throw new Error("history failed")
        }
        return await response.json() as [GamePreView]
    }

    async getGame(gameId: string) {
        const response = await this.call(`/api/game/${gameId}`)
        if (!response.ok) {
            throw new Error("getGame failed")
        }
        return await response.json() as Game
    }

    async getUser(username: string) {
        const response = await this.call(`/api/user/${username}`)
        if (!response.ok) {
            throw new Error("getGame failed")
        }
        return await response.json() as User
    }

    async move(gameId: string, move: Move) {
        const response = await this.call(`/api/game/${gameId}/move`, 'POST', JSON.stringify(move))
        if (!response.ok) {
            throw new Error("move failed")
        }
        return await response.json() as Game
    }
}
