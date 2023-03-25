<script lang="ts">
    import {getToken} from "../../lib/auth/auth";
    import {directLogin} from "../../lib/api/gameAPI";

    const noToken = getToken() === undefined

    if (noToken) {
        directLogin();
    }
</script>

{#if !noToken}
    <header>
        <button id="new-game-button" type="button">New Game</button>
        <button id="logout-button" type="button">Logout</button>
    </header>

    <main>
        <slot/>
    </main>
{/if}

<style>
    :root {
        font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
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

    header {
        display: flex;
        justify-content: space-between;
    }

    #new-game-button {
        align-self: start;
    }
</style>
