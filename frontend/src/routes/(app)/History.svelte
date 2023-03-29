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

    onMount(() => {
        promise = gameApi.history();
    })
</script>

<table id="user-history-table">
    {#await promise}
        <p>...waiting</p>
    {:then history}
        {#each history as gameView}
            <tr>
                <td class="user-history">
                    <div>
                        <p>Players: {gameView.players.map(x => x.username).join(", ")}</p>
                        {#if gameView.winner !== null}
                            <p>Winner: {gameView.winner.username}</p>
                            <button class="view-history-button" type="button">View game history</button>
                        {:else}
                            <button class="view-history-button" type="button" on:click={() => playGame(gameView.id)}>
                                Play
                            </button>
                        {/if}
                    </div>
                </td>
            </tr>
        {/each}
    {:catch error}
        <p style="color: red">{error.message}</p>
    {/await}
</table>

<style>
    td.user-history {
        border: 1px solid dimgray;
        border-collapse: collapse;
        padding: 0 10px 10px 10px;
    }

    #user-history-table {
        margin-left: auto;
        margin-right: auto;
    }
</style>