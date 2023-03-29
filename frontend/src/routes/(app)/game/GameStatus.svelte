<script lang="ts">
    import type {Players} from "$lib/api/types";
    import {sideOrder} from "./types";

    export let players: Players;

    $: playersList = [players.activePlayer, ...players.enemies].sort((a, b) => {
        return sideOrder(a.target) - sideOrder(b.target)
    });
</script>

<ul>
    {#each playersList as {username, target, wallsAmount} (target)}
        {#if target === players.activePlayer.target}
            <li class={target}>➤ {username}: {wallsAmount}</li>
        {:else}
            <li class={target}>{username}: {wallsAmount}</li>
        {/if}
    {/each}
</ul>

<style>
    ul {
        list-style: none;
        width: max-content;
        float: left;
    }

    li::before {
        content: "\2B24";
        font-weight: bold;
        padding-bottom: 0;
        padding-top: 0;
    }

    li {
        font-size: x-large;
        padding-bottom: 0;
        padding-top: 0;
    }

    li.north:before {
        color: #0000FF;
    }

    li.south:before {
        color: #FF0000;
    }


    ul li.east:before {
        color: #00FF00;
    }


    ul li.west:before {
        color: #FFFF00;
    }
</style>