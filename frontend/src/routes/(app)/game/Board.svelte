<script lang="ts">
    import Quoridor from "./Quoridor.svelte";
    import Cell from "./Cell.svelte";
    import Pawn from "./Pawn.svelte";
    import {rotationAngle} from "./types.js";
    import type {Player} from "$lib/api/types";
    import {onMount} from "svelte";
    import Plug from "./Plug.svelte";
    import type {Claim} from "$lib/auth/auth";
    import type {Game, PawnPosition} from "$lib/api/types";
    import EmptyCell from "./EmptyCell.svelte";

    export let onMove: (Move) => {};
    export let game: Game;
    export let pawnMoves: [PawnPosition] = [];
    export let user: Claim;
    export let qd: number;

    let target;
    let qsh;
    let qsv;
    const cs = [...Array(9).keys()];

    $: state = game.state;
    $: walls = state.walls;
    $: players = [state.players.activePlayer, ...state.players.enemies];

    $: max = 8 * qd + 9 * qd * 3;

    $: cd = 3 * qd;
    $: h = 8 * qd + 9 * cd;

    onMount(() => {
        target = players.find<Player>(p => {
            return p.id === user.userId;
        })?.target;

        if (["east", "south"].includes(target)) {
            qsv = [...Array(8).keys()].reverse();
        } else {
            qsv = [...Array(8).keys()];
        }

        if (["west", "south"].includes(target)) {
            qsh = [...Array(8).keys()].reverse();
        } else {
            qsh = [...Array(8).keys()];
        }
    })
</script>

{#if target !== undefined}
    <svg width={max} height={max} transform="rotate({rotationAngle(target)})">
        <rect width={h} height={h} class="background"/>
        {#each cs as i}
            {#each cs as j}
                <EmptyCell row={i} column={j} bind:cd={cd} bind:qd={qd}/>
            {/each}
        {/each}

        {#key pawnMoves}
            {#each pawnMoves as pawnPosition}
                {@const pawn = {pawnPosition: pawnPosition}}
                <Cell row={pawnPosition.row} column={pawnPosition.column} bind:cd={cd} bind:qd={qd}
                      on:click={() => onMove(pawn)} target={target}/>
            {/each}
        {/key}

        {#if state.players.activePlayer.id === user.userId && game.winner === null }
            {#each qsv as i}
                {#each qsh as j}
                    {@const wall = {orientation: 'horizontal', row: i, column: j}}
                    <Quoridor wall={wall} bind:qd={qd} bind:cd={cd} status='empty'
                              on:click={() => onMove({wallPosition: wall})}
                    />
                {/each}
            {/each}

            {#each qsv as i}
                {#each qsh as j}
                    {@const wall = {orientation: 'vertical', row: i, column: j}}
                    <Quoridor wall={wall} bind:qd={qd} bind:cd={cd} status='empty'
                              on:click={() => onMove({wallPosition: wall})}
                    />
                {/each}
            {/each}
        {/if}

        {#each qsv as i}
            {#each qsh as j}
                <Plug row={i} column={j} bind:cd={cd} bind:qd={qd}></Plug>
            {/each}
        {/each}

        {#key walls}
            {#each walls as wall}
                <Quoridor wall={wall} bind:qd={qd} bind:cd={cd} status='wall'/>
            {/each}
        {/key}

        {#key players}
            {#each players as {pawnPosition, target}}
                <Pawn row={pawnPosition.row} column={pawnPosition.column} target={target} bind:cd={cd} bind:qd={qd}/>
            {/each}
        {/key}
    </svg>
{/if}

<style>
    rect.background {
        fill: gray;
    }
</style>