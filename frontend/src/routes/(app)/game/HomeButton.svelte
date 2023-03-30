<script>
    import {browser} from "$app/environment";
</script>
<button type="button" on:click={() => {browser && window.location.assign('/');}}>Home</button>

<style>
    button {
        box-shadow: 4px 4px 8px 0 rgba(0, 0, 0, 0.3), 0 3px 6px 0 rgba(0, 0, 0, 0.2);
    }
</style>