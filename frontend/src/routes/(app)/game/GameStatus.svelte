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
            <li class="{target} active">{username}: {wallsAmount}</li>
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
        padding-left: 1.6em;
    }

    li.active::before {
        content: "● ";
        font-size: 1em;
    }

    li:not(.active)::before {
        content: "○ ";
        font-size: 1em;
    }

    li::before {
        font-weight: bold;
    }

    li {
        font-size: x-large;
    }

    li.north::before {
        color: #3434e6;
    }

    li.south::before {
        color: #bc1313;
    }

    li.east::before {
        color: #38ae38;
    }

    li.west::before {
        color: #c8c826;
    }

    @media screen and (max-width: 961px) {
        ul {
            columns: 2;
            gap: 2em;
        }

        li {
            font-size: inherit;
        }

        li.active::before {
            content: "● ";
            font-size: inherit;
        }

        li:not(.active)::before {
            content: "○ ";
            font-size: inherit;
        }
    }
</style>