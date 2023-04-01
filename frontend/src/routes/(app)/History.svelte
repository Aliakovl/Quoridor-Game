<script lang="ts">
    import {GameAPI} from "$lib/api/gameAPI";
    import {onMount} from "svelte";
    import {browser} from "$app/environment";

    export let gameApi: GameAPI;

    let promise = Promise.resolve([]);

    function playGame(gameId: string) {
        browser && sessionStorage.setItem("gameId", gameId);
        window.location.assign('/game');
    }

    function viewHistory(gameId: string) {
        browser && sessionStorage.setItem("gameId", gameId);
        window.location.assign('/history');
    }

    onMount(() => {
        promise = gameApi.history();
    })
</script>

<div class="user-history-table">
    {#await promise}
        <p>...waiting</p>
    {:then history}
        {#each history as gameView}
            <div class="user-history">
                <div class="players-context">
                    <p>Players: {gameView.players.map(x => x.username).join(", ")}</p>
                    {#if gameView.winner !== null}
                        <p>Winner: {gameView.winner.username}</p>
                    {/if}
                </div>
                {#if gameView.winner !== null}
                    <button class="view-history-button" type="button"
                            on:click={() => viewHistory(gameView.id)}>
                        View game history
                    </button>
                {:else}
                    <button class="view-history-button" type="button"
                            on:click={() => playGame(gameView.id)}>
                        Play
                    </button>
                {/if}
            </div>
        {/each}
    {:catch error}
        <p style="color: red">{error.message}</p>
    {/await}
</div>

<style>
    div.user-history-table {
        display: flex;
        justify-content: center;
        flex-flow: row wrap;
        align-content: start;
        gap: 1rem;
    }

    .user-history {
        flex: 1 1 0;
        justify-content: space-between;
        display: flex;
        flex-flow: column;
        min-width: 13rem;
        max-width: 13rem;
        border: 2px solid dimgray;
        background-color: dimgray;
        color: ghostwhite;
        border-radius: 0.7em;
        border-collapse: collapse;
        padding: 0 10px 10px 10px;
        overflow-wrap: break-word;
        text-align: center;
    }

    button {
        background-color: #2a2a2a;
        color: ghostwhite;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.1);
    }
</style>