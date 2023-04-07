<script lang="ts">
    import type {WallPosition} from "$lib/api/types";

    export let wall: WallPosition
    export let qd: number;
    export let cd: number;
    export let status: QuoridorStatus;

    const i = wall.row;
    const j = wall.column;

    let x: number = 0;
    let y: number = 0;
    let width: number = 0;
    let height: number = 0;

    $: if (wall.orientation == "horizontal") {
        x = (cd + qd) * j;
        y = cd * (i + 1) + qd * i;
        width = 2 * cd + qd;
        height = qd;
    } else if (wall.orientation == "vertical") {
        x = cd * (i + 1) + qd * i;
        y = (cd + qd) * j;
        width = qd;
        height = 2 * cd + qd;
    }
</script>

{#if status === 'empty'}
    <rect x={x} y={y} width={width} height={height} on:click class={status}></rect>
{:else }
    <rect x={x} y={y} width={width} height={height} class={status}></rect>
{/if}

<style>
    rect.empty {
        fill: transparent;
    }

    rect.forbidden {
        fill: transparent;
    }

    rect.wall {
        fill: #377469;
        stroke-width: 1;
        stroke: #212121;
    }

    @media (hover: hover) {
        rect.empty:hover {
            fill: #d8d8d8;
            stroke-width: 1;
            stroke: #212121;
        }
    }
</style>