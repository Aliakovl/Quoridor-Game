<script lang="ts">
    export let row: number;
    export let column: number;
    export let cd: number;
    export let qd: number;

    $: x = (cd + qd) * column + cd;
    $: y = (cd + qd) * row + cd;
    $: d = qd;
</script>

<rect x={x} y={y} width={d} height={d} on:click></rect>

<style>
    rect {
        fill: transparent;
    }
</style>