<script lang="ts">
    import '$lib/forms.css';
    import {GameAPI} from "$lib/api/gameAPI";
    import {browser} from "$app/environment";

    export let gameAPI: GameAPI;
    export let gameId: string;
    export let creator: string;

    let styles = {
        'bg': 'transparent'
    };

    $: cssVarStyles = Object.entries(styles)
        .map(([key, value]) => `--${key}:${value}`)
        .join(';');

    let players = [creator];

    let username = "";

    async function handleAddPlayer(event) {
        const user = await gameAPI.getUser(username).catch(() => {
            styles.bg = "#AA0000";
        })
        await gameAPI.joinPlayer(gameId, user.id).then(() => {
            styles.bg = "transparent";
            players = players.concat(username);
            username = "";
        }).catch(() => {
            styles.bg = "#AA0000";
        })
    }

    async function handleStartGame(event) {
        await gameAPI.startGame(gameId);
        browser && sessionStorage.setItem("gameId", gameId);
        browser && window.location.assign('/game');
    }
</script>

<div id="join">
    <form id="create-game-form" on:submit|preventDefault>
        <div class="field" style="{cssVarStyles}">
            <input bind:value={username} id="user-login" type="text" name="username" form="create-game-form"
                   placeholder="Player's username"
                   required/>
        </div>
        <div class="submits">
            <input id="add-player" type="submit" value="Add player" form="create-game-form" on:click={handleAddPlayer}/>
            <input id="start-game" type="button" value="Start game" on:click={handleStartGame}/>
        </div>
    </form>
    <div id="players">
        <label>Participants:</label>
        <ul id="added-players">
            {#each players as player}
                <li>{player}</li>
            {/each}
        </ul>
    </div>
</div>

<style>
    #players {
        margin-top: 2em;
    }

    #user-login {
        border-color: var(--bg);
    }

    #user-login:hover {
        border-color: #646cff;
    }
</style>