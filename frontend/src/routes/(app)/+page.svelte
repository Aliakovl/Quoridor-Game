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

<header>
    <LogoutButton>
        <NewGameButton func={createGame}/>
    </LogoutButton>
</header>
<main>
    <div id="app">
        {#if gameId !== undefined}
            <JoinPlayers gameAPI={gameApi} gameId={gameId} creator={username}/>
        {/if}
    </div>
    <History gameApi={gameApi}/>
</main>

<style>
    :root {
        font-family: ui-monospace;
        font-size: 1.1em;
        font-weight: 500;
    }

    header {
        margin-bottom: 5em;
    }

    #app {
        justify-content: center;
        display: flex;
        place-items: baseline;
        text-align: center;
    }

    main {
        display: grid;
        grid-template-columns: 1fr 1fr;
    }
</style>