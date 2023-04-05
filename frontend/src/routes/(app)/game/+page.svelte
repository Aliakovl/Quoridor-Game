<script lang="ts">
    import '$lib/forms.css';
    import Board from "./Board.svelte";
    import type {Game, Move} from "$lib/api/types";
    import {GameWS} from "$lib/api/gameWS";
    import {onMount} from "svelte";
    import {browser} from "$app/environment";
    import {directHome, saveToken} from "$lib/auth/auth";
    import {refresh} from "$lib/auth/authAPI";
    import {getUser} from "$lib/auth/auth";
    import type {User} from "$lib/auth/auth";
    import GameStatus from "./GameStatus.svelte";
    import LogoutButton from "../LogoutButton.svelte";
    import HomeButton from "./HomeButton.svelte";

    let gameWS: GameWS;
    let game: Game;
    let user: User;

    onMount(async () => {
        const gameId = browser && sessionStorage.getItem("gameId") || undefined
        if (gameId === undefined) {
            directHome();
        } else {
            const token = await refresh();
            user = getUser(token);
            saveToken(token);
            gameWS = new GameWS(gameId, token, update);
        }
    })

    function update(newGame: Game) {
        game = newGame;
    }

    function onMove(move: Move) {
        gameWS.send(move);
    }

    let innerWidth;
    let innerHeight;
    let width
    $: if (innerWidth > 961) {
        width = Math.min(innerWidth, innerHeight) - 50
    } else {
        width = Math.min(innerWidth, innerHeight - 200)
    }
    $: qt = width / 45
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
            <div class="placeholder"></div>
            <div class="board" style="padding: {3*qt}px; border-radius: {1.61803398875 * qt}px">
                <Board onMove={onMove} user={user} bind:state={game.state} qd={qt}/>
            </div>
            <div class="placeholder">
                <div class="status" style="margin-top: {3*qt}px; margin-bottom: {3*qt}px">
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

    @media screen and (max-width: 961px) {
        .app {
            display: flex;
            flex-flow: column-reverse;
        }

        .status {
            padding: 0 0.8em 0 0;
            border-radius: 0.6em;
        }
    }
</style>