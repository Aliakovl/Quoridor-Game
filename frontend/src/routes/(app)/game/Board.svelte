<script lang="ts">
    import type {State} from "$lib/api/types";
    import Quoridor from "./Quoridor.svelte";
    import Cell from "./Cell.svelte";

    export let onMove: (Move) => {};
    export let state: State;

    $: walls = state.walls;

    let max_qd = 20;
    let max = 8 * max_qd + 9 * max_qd * 3 + 100;

    let qd = max_qd;
    $: cd = 3 * qd;
    $: h = 8 * qd + 9 * cd;

    const qs = [...Array(8).keys()];
    const cs = [...Array(9).keys()];
</script>

<svg width={max} height={max}>
    <rect width={h} height={h} class="background"/>
    {#each cs as i}
        {#each cs as j}
            {@const pawn = {pawnPosition: {row: i, column: j}}}
            <Cell i={i} j={j} bind:cd={cd} bind:qd={qd}
                  on:click={() => onMove(pawn)}/>
        {/each}
    {/each}

    {#each qs as i}
        {#each qs as j}
            {@const wall = {orientation: 'horizontal', row: i, column: j}}
            <Quoridor wall={wall} bind:qd={qd} bind:cd={cd} status='empty'
                      on:click={() => onMove({wallPosition: wall})}
            />
        {/each}
    {/each}

    {#each qs as i}
        {#each qs as j}
            {@const wall = {orientation: 'vertical', row: i, column: j}}
            <Quoridor wall={wall} bind:qd={qd} bind:cd={cd} status='empty'
                      on:click={() => onMove({wallPosition: wall})}
            />
        {/each}
    {/each}

    {#key walls}
        {#each walls as wall}
            <Quoridor wall={wall} bind:qd={qd} bind:cd={cd} status='wall'/>
        {/each}
    {/key}
</svg>

<style>
    rect.background {
        fill: gray;
    }
</style>