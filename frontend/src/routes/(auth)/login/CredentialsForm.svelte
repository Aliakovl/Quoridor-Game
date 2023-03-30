<script lang="ts">
    import {signIn, signUp} from "$lib/auth/authAPI";
    import {saveToken, directHome} from "$lib/auth/auth";

    let username = "";
    let password = "";

    async function signInHandler(event) {
        await signIn(username, password).then(accessToken => {
            saveToken(accessToken);
            directHome();
        })
    }

    async function signUpHandler(event) {
        await signUp(username, password).then(accessToken => {
            saveToken(accessToken);
            directHome();
        })
    }
</script>

<div class="form">
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
</div>

<style>
    .submits {
        display: flex;
        justify-content: space-between;
        align-content: stretch;
        gap: 1em;
    }

    .submits > input {
        width: 100%;
        height: 100%;
    }

    #username-field {
        margin-bottom: 0.4em;
    }

    #password-field {
        margin-bottom: 0.8em;
    }

    input {
        font-family: ui-monospace;
        border-radius: 8px;
        border: 1px solid transparent;
        padding: 0.6em 1.2em;
        font-size: 1.1em;
        font-weight: 500;
        background-color: #1a1a1a;
        color: lightgray;
        cursor: pointer;
        transition: border-color 0.25s;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.1);
    }

    input:hover {
        border-color: #646cff;
    }

    input:focus,
    input:focus-visible {
        outline: 4px auto -webkit-focus-ring-color;
    }

    .form {
        background-color: dimgray;
        padding: 1.2em;
        border-radius: 0.7em;
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 6px 10px 0 rgba(0, 0, 0, 0.2);
    }
</style>