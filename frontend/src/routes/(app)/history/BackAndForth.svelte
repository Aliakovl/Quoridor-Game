<script lang="ts">
    export let index: number;
    export let max: number;

    function onBack() {
        if (index > 0) {
            index -= 1;
        }
    }

    function onForth() {
        if (index < max) {
            index += 1;
        }
    }

    function onKey(event: KeyboardEvent) {
        if (event.code === "ArrowRight") {
            onForth();
        } else if (event.code == "ArrowLeft") {
            onBack();
        }
    }
</script>

<svelte:window on:keydown={onKey}/>

<div>
    <button on:click={onBack}>◀</button>
    <button on:click={onForth}>▶</button>
</div>

<style>
    button {
        font-size: 1.5em;
    }

    div {
        display: flex;
        gap: 1em;
        background-color: #747474;
        border-radius: 0.8em;
        padding: 1.2em;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
    }
</style>