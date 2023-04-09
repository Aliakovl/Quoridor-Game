<script lang="ts">
    export let index: number;
    export let max: number;

    let leftDisabled = true;
    let rightDisabled = false;

    function onBack() {
        index -= 1;
        if (index <= 0) {
            leftDisabled = true;
        }
        rightDisabled = false;
    }

    function onForth() {
        index += 1;
        if (index >= max) {
            rightDisabled = true;
        }
        leftDisabled = false
    }

    function onKey(event: KeyboardEvent) {
        if (event.code === "ArrowRight") {
            if (!rightDisabled) {
                onForth();
            }
        } else if (event.code == "ArrowLeft") {
            if (!leftDisabled) {
                onBack();
            }
        }
    }
</script>

<svelte:window on:keydown={onKey}/>

<div>
    <button on:click={onBack} disabled={leftDisabled}>◀</button>
    <button on:click={onForth} disabled={rightDisabled}>▶</button>
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

    @media screen and (max-width: 961px) {
        button {
            font-size: inherit;
        }

        button:disabled {
            background: #444444;
        }

        div {
            display: flex;
            gap: 1em;
            background-color: #747474;
            border-radius: 0.8em;
            padding: 0.8em;
            box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
            margin-top: 1em;
        }
    }
</style>