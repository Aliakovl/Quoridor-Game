<script lang="ts">
    export let row: number;
    export let column: number;
    export let cd: number;
    export let qd: number;

    $: x = (cd + qd) * column;
    $: y = (cd + qd) * row;
    $: d = cd;
</script>

<rect x={x} y={y} width={d} height={d}></rect>

<style>
    rect {
        fill: #515151;
        stroke-width: 1;
        stroke: #212121;
    }
</style>