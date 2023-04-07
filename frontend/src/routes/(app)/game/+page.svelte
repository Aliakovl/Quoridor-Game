<script lang="ts">
    import '$lib/forms.css';
    import Board from "./Board.svelte";
    import type {Game, Move} from "$lib/api/types";
    import {GameWS} from "$lib/api/gameWS";
    import {onMount} from "svelte";
    import {browser} from "$app/environment";
    import {directHome, saveToken, getUser} from "$lib/auth/auth";
    import {refresh} from "$lib/auth/authAPI";
    import type {Claim} from "$lib/auth/auth";
    import GameStatus from "./GameStatus.svelte";
    import LogoutButton from "../LogoutButton.svelte";
    import HomeButton from "./HomeButton.svelte";
    import Modal from "./Modal.svelte";
    import type {PawnPosition} from "$lib/api/types";
    import {GameAPI} from "$lib/api/gameAPI";
    import type {WallPosition} from "$lib/api/types";

    let gameWS: GameWS;
    let game: Game;
    let user: Claim;
    let gameId: string;
    let pawnMoves: [PawnPosition];
    let wallMoves: [WallPosition];
    let collapsed = false;
    let gameAPI: GameAPI;

    onMount(async () => {
        const _gameId = browser && sessionStorage.getItem("gameId") || undefined;
        if (_gameId === undefined) {
            directHome();
        } else {
            gameId = _gameId;
            const token = await refresh();
            user = getUser(token);
            gameAPI = new GameAPI(token);
            saveToken(token);
            gameWS = new GameWS(gameId, token, update);
        }
    })

    async function update(newGame: Game) {
        game = newGame;
        if (game.state.players.activePlayer.id === user.userId) {
            pawnMoves = await gameAPI.pawnMoves(gameId).catch(() => []);
            wallMoves = await gameAPI.wallMoves(gameId).catch(() => []);
        } else {
            pawnMoves = []
        }
    }

    function onMove(move: Move) {
        gameWS.send(move);
    }

    function collapse(event) {
        collapsed = true;
    }

    let innerWidth;
    let innerHeight;
    let width
    $: if (innerWidth > 961) {
        width = Math.min(innerWidth, innerHeight) - 50
    } else {
        width = Math.min(innerWidth, innerHeight - 200)
    }
    $: qd = width / 45
</script>

<svelte:window bind:innerWidth={innerWidth} bind:innerHeight={innerHeight}></svelte:window>

<header>
    <LogoutButton>
        <HomeButton/>
    </LogoutButton>
</header>
<main>
    <div class="app">
        {#if game !== undefined && user !== undefined}
            {#if game.winner !== null && !collapsed}
                {#if user.userId === game.winner.id}
                    <Modal>
                        <p class="win-text">You won!</p>
                        <button class="win-button" on:click={collapse}>Yippee!</button>
                    </Modal>
                {:else }
                    <Modal>
                        <p class="win-text">Winner - {game.winner.username}!</p>
                        <button class="win-button" on:click={collapse}>Let it go</button>
                    </Modal>
                {/if}
            {/if}
            <div class="placeholder"></div>
            <div class="board" style="padding: {3*qd}px; border-radius: {1.61803398875 * qd}px">
                <Board onMove={onMove} user={user} bind:game={game} bind:pawnMoves={pawnMoves}
                       bind:wallMoves={wallMoves} qd={qd}/>
            </div>
            <div class="placeholder">
                <div class="status" style="margin-top: {3*qd}px; margin-bottom: {3*qd}px">
                    <GameStatus players={game.state.players}/>
                </div>
            </div>
        {/if}
    </div>
</main>

<style>
    :root {
        background-color: #212121;
        color: lightgray;
        font-family: ui-monospace;
        font-size: 1.1em;
        font-weight: 500;
    }

    div {
        justify-content: center;
        display: flex;
        place-items: center;
    }

    .board {
        background-color: gray;
        margin-top: 1em;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
    }

    .placeholder {
        flex: 1;
    }

    .status {
        flex: 1;
        padding: 0 1em 0 1em;
        max-width: min-content;
        margin-left: 2em;
        margin-right: 2em;
        background-color: #747474;
        border-radius: 0.8em;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
    }

    .app {
        flex: 1;
    }

    .win-button {
        flex: 1;
    }

    .win-text {
        text-align: center;
        flex: 1;
    }

    @media screen and (max-width: 961px) {
        .app {
            display: flex;
            flex-flow: column-reverse;
        }

        .status {
            padding: 0 0.8em 0 0;
            border-radius: 0.6em;
        }

        .win-button {
            font-size: large;
        }

        .win-text {
            font-size: large;
        }
    }
</style>