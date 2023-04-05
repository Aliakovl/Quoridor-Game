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
    <div class="app">
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

    .app {
        justify-content: center;
        display: flex;
        place-items: baseline;
        text-align: center;
    }

    main {
        display: grid;
        grid-template-columns: 1fr 1fr;
        margin-top: 5em;
    }

    @media screen and (max-width: 961px) {
        main {
            display: flex;
            flex-direction: column;
            margin-top: 3em;
        }
    }
</style>