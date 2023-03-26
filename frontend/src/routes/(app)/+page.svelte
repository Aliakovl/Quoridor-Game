<script lang="ts">
    import {GameAPI} from "$lib/api/gameAPI";
    import History from "./History.svelte";
    import JoinPlayers from "./JoinPlayers.svelte";
    import NewGameButton from "./NewGameButton.svelte";
    import LogoutButton from "./LogoutButton.svelte";

    const gameApi = GameAPI.Unsafe();

    let gameId = undefined;
    let username = undefined;

    async function createGame(event) {
        const game = await gameApi.createGame();
        gameId = game.id;
        username = game.players.creator.username;
    }
</script>


<LogoutButton>
    <NewGameButton func={createGame}/>
</LogoutButton>
<main>
    <div id="app">
        {#if gameId !== undefined}
            <JoinPlayers gameAPI={gameApi} gameId={gameId} creator={username}/>
        {/if}
        <History gameApi={gameApi}/>
    </div>
</main>

<style>
    #app {
        justify-content: center;
        display: flex;
        place-items: baseline;
        text-align: center;
    }
</style>