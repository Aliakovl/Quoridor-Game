<script lang="ts">
    import {deleteToken} from "$lib/auth/auth";
    import {directLogin} from "$lib/api/gameAPI";
    import {signOut} from "$lib/auth/authAPI";

    async function logout(event) {
        await signOut().catch(() => {
        }).finally(deleteToken);
        directLogin();
    }
</script>

<header>
    <slot/>
    <button id="logout-button" type="button" on:click|once={logout}>Logout</button>
</header>

<style>
    header {
        display: flex;
        justify-content: space-between;
    }
</style>