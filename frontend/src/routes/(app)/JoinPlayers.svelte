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
                   placeholder="Username" required autocomplete="off"/>
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
    #join {
        border: 2px solid dimgray;
        background-color: dimgray;
        color: ghostwhite;
        border-radius: 0.7em;
        padding: 0 10px 10px 10px;
    }

    #players {
        margin-top: 2em;
        background-color: #2a2a2a;
        border-radius: 0.7em;
        padding: 12px;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.1);
    }

    #user-login {
        border-color: var(--bg);
    }

    .field > input {
        background-color: #414141;
        color: ghostwhite;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.1);
    }

    .submits > input {
        background-color: #2a2a2a;
        color: ghostwhite;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.1);
    }

    #user-login:hover {
        border-color: #646cff;
    }

    li {
        text-align: left;
    }
</style>