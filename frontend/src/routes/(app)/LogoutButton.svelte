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

    button {
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 3px 6px 0 rgba(0, 0, 0, 0.2);
    }
</style>