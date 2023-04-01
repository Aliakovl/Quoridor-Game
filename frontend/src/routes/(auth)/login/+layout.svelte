<script lang="ts">
    import {refresh} from "$lib/auth/authAPI";
    import {saveToken, directHome} from "$lib/auth/auth";
    import {onMount} from "svelte";
    import Spinner from "$lib/Spinner.svelte";

    let logged = true

    onMount(async () => {
        logged = await refresh().then(accessToken => {
            saveToken(accessToken)
            directHome()
            return true
        }).catch(() => false)
    })
</script>

{#if !logged}
    <main>
        <slot/>
    </main>
{:else}
    <main>
        <Spinner/>
    </main>
{/if}

<style>
    :root {
        font-family: ui-monospace;
        line-height: 1.5;
        font-weight: 600;
        min-width: 0;
        color-scheme: light dark;
        color: rgba(255, 255, 255, 0.87);
        background-color: #242424;
        font-synthesis: none;
        text-rendering: optimizeLegibility;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        -webkit-text-size-adjust: 100%;
    }

    h1 {
        font-size: 3.2em;
        line-height: 1.1;
    }
</style>
