<script lang="ts">
    export let func: () => {}
</script>

<button id="new-game-button" type="button" on:click|once={func}>New Game</button>

<style>
    #new-game-button {
        align-self: start;
    }
</style>