<script lang="ts">
    export let i: number;
    export let j: number;
    export let cd: number;
    export let qd: number;

    $: x = (cd + qd) * j;
    $: y = (cd + qd) * i;
    $: d = cd;
</script>

<rect x={x} y={y} width={d} height={d} on:click></rect>

<style>
    rect {
        fill: #515151;
    }

    rect:hover {
        fill: #646cff;
    }
</style>