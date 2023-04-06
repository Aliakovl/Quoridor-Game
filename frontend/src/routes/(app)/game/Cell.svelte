<script lang="ts">
    import type {Side} from "../../../lib/api/types";

    export let row: number;
    export let column: number;
    export let cd: number;
    export let qd: number;
    export let target: Side;

    $: x = (cd + qd) * column;
    $: y = (cd + qd) * row;
    $: d = cd;
</script>

<rect x={x} y={y} width={d} height={d} on:click class={target}></rect>

<style>
    rect {
        stroke-width: 1;
        stroke: #212121;
    }

    rect.north {
        fill: rgba(52, 52, 230, 0.2);
    }

    rect.south {
        fill: rgba(188, 19, 19, 0.2);
    }

    rect.east {
        fill: rgba(56, 174, 56, 0.2);
    }

    rect.west {
        fill: rgba(200, 200, 38, 0.2);
    }

    @media (hover: hover) {
        rect:hover {
            fill: #d8d8d8;
            stroke-width: 1;
            stroke: #212121;
        }
    }
</style>