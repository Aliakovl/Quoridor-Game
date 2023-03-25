<script lang="ts">
    import {signIn, signUp} from "../../../lib/auth/authAPI";
    import {saveToken} from "../../../lib/auth/auth";
    import {browser} from "$app/environment";

    let username = "";
    let password = "";

    function directHome() {
        if (browser) {
            window.location.replace('/');
        }
    }

    async function signInHandler(event) {
        await signIn(username, password).then(accessToken => {
            saveToken(accessToken);
            directHome();
        }).catch(console.log)
    }

    async function signUpHandler(event) {
        await signUp(username, password).then(accessToken => {
            saveToken(accessToken);
            directHome();
        }).catch(console.log)
    }
</script>

<form id="credentials" on:submit|preventDefault>
    <div class="field">
        <input bind:value={username} id="username-field" type="text" name="username" form="credentials" required
               placeholder="Username"/>
    </div>
    <div class="field">
        <input bind:value={password} id="password-field" type="password" name="password" form="credentials"
               required
               placeholder="Password"/>
    </div>
    <div class="submits">
        <input id="sign-in" type="submit" value="Sign In" form="credentials" on:click={signInHandler}/>
        <input id="sign-up" type="submit" value="Sign Up" form="credentials" on:click={signUpHandler}/>
    </div>
</form>

<style>
    .submits {
        display: flex;
        justify-content: space-between;
        align-content: stretch;
        gap: 1em;
        margin-top: 0.6em;
    }

    .submits > input {
        width: 100%;
        height: 100%;
    }

    .field {
        margin-top: 0.4em;
    }

    input {
        font-family: ui-monospace;
        border-radius: 8px;
        border: 1px solid transparent;
        padding: 0.6em 1.2em;
        font-size: 1em;
        font-weight: 500;
        background-color: #1a1a1a;
        cursor: pointer;
        transition: border-color 0.25s;
    }

    input:hover {
        border-color: #646cff;
    }

    input:focus,
    input:focus-visible {
        outline: 4px auto -webkit-focus-ring-color;
    }
</style>