<script lang="ts">
    import {getToken} from "$lib/auth/auth";
    import {directLogin} from "$lib/api/gameAPI";
    import Spinner from "$lib/Spinner.svelte";

    const noToken = getToken() === undefined;

    if (noToken) {
        directLogin();
    }
</script>

{#if !noToken}
    <slot/>
{:else}
    <Spinner/>
{/if}

<style>
    :root {
        font-family: ui-monospace;
        line-height: 1.5;
        font-weight: 600;
        min-width: 0;
        color-scheme: light dark;
        color: lightgray;
        background-color: #212121;
        font-synthesis: none;
        text-rendering: optimizeLegibility;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        -webkit-text-size-adjust: 100%;
    }
</style>