<script lang="ts">
    import type {Side} from "$lib/api/types";
    import {pawnColour} from "./types.js";

    export let row: number;
    export let column: number;
    export let target: Side;
    export let cd: number;
    export let qd: number;

    $: x = (cd + qd) * column;
    $: y = (cd + qd) * row;
    $: d = cd;
</script>

<rect x={x} y={y} width={d} height={d} style:fill={pawnColour(target)}></rect>

<style>
    rect {
        stroke-width: 1;
        stroke: #212121;
    }
</style>