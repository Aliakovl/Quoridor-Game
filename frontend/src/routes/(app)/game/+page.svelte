<script lang="ts">
    import Board from "./Board.svelte";
    import type {Game, Move} from "$lib/api/types";
    import {GameWS} from "$lib/api/gameWS";
    import {onMount} from "svelte";
    import {browser} from "$app/environment";
    import {directHome, saveToken} from "$lib/auth/auth";
    import {refresh} from "$lib/auth/authAPI";

    let gameId;
    let gameWS: GameWS;
    let game: Game;

    onMount(async () => {
        gameId = browser && sessionStorage.getItem("gameId") || undefined
        if (gameId === undefined) {
            directHome()
        } else {
            const token = await refresh()
            saveToken(token)
            gameWS = new GameWS(gameId, token, update)
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

<div>
    <div></div>
    <div>
        {#if game !== undefined}
            <Board onMove={onMove} bind:state={game.state}/>
        {/if}
    </div>
    <div></div>
</div>

<style>
    :root {
        background-color: #1a1a1a;
        color: lightgray;
    }

    div {
        justify-content: center;
        display: flex;
        place-items: center;
        text-align: center;
    }
</style>