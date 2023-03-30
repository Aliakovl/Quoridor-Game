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
            console.log(user);
            saveToken(token);
            gameWS = new GameWS(gameId, token, update);
        }
    })

    function update(newGame: Game) {
        game = newGame;
    }

    function onMove(move: Move) {
        console.log(move);
        gameWS.send(move);
    }
</script>

<main>
    <LogoutButton>
        <HomeButton/>
    </LogoutButton>
    <div class="app">
        {#if game !== undefined && user !== undefined}
            <div class="placeholder"></div>
            <div class="board">
                <Board onMove={onMove} user={user} bind:state={game.state}/>
            </div>
            <div class="placeholder">
                <div class="status">
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
        padding: 3em;
        background-color: gray;
        border-radius: 2em;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
    }

    .placeholder {
        flex: 1;
    }

    .status {
        flex: 1;
        padding: 0 1em 0 1em;
        max-width: min-content;
        background-color: #747474;
        border-radius: 0.8em;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
    }

    .app {
        flex: 1;
    }
</style>