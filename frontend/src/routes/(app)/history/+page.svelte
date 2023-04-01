<script lang="ts">
    import '$lib/forms.css';
    import LogoutButton from "../LogoutButton.svelte";
    import HomeButton from "../game/HomeButton.svelte";
    import {onMount} from "svelte";
    import {browser} from "$app/environment";
    import {directHome, getToken} from "$lib/auth/auth";
    import {GameAPI} from "$lib/api/gameAPI";
    import {getUser} from "$lib/auth/auth";
    import Board from "../game/Board.svelte";
    import type {Game} from "$lib/api/types";
    import GameStatus from "../game/GameStatus.svelte";
    import BackAndForth from "./BackAndForth.svelte";

    let history: [Game];
    let gameApi: GameAPI;
    let user;
    let index = 0;

    onMount(async () => {
        const gameId = browser && sessionStorage.getItem("gameId") || undefined
        const token = getToken();
        if (gameId === undefined || token === undefined) {
            directHome();
        } else {
            gameApi = new GameAPI(token);
            user = getUser(token);
            history = await gameApi.gameHistory(gameId);
        }
    })
</script>

<header>
    <LogoutButton>
        <HomeButton/>
    </LogoutButton>
</header>
<main>
    <div class="app">
        {#if history !== undefined}
            <div class="placeholder">
                <BackAndForth bind:index={index} max={history.length - 1}/>
            </div>
            <div class="board">
                <Board onMove={() => {}} user={user} bind:state={history[index].state}/>
            </div>
            <div class="placeholder">
                <div class="status">
                    <GameStatus players={history[index].state.players}/>
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