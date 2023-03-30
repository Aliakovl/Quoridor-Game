<script lang="ts">
    export let func: () => {}
</script>

<button id="new-game-button" type="button" on:click|once={func}>New Game</button>

<style>
    #new-game-button {
        align-self: start;
    }

    button {
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 3px 6px 0 rgba(0, 0, 0, 0.2);
    }
</style>