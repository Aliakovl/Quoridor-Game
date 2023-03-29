<script lang="ts">
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

<div>
    <div></div>
    {#if game !== undefined && user !== undefined}
        <div>
            <Board onMove={onMove} user={user} bind:state={game.state}/>
        </div>
        <div>
            <GameStatus players={game.state.players}/>
        </div>
    {/if}
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
        text-align: left;
    }
</style>