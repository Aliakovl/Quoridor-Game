<script lang="ts">
    import CredentialsForm from "./CredentialsForm.svelte";

    export let data: {
        description: string;
    };
</script>

<div class="app">
    <div class="description">
        {@html data.description}
    </div>
    <div class="credentials-form">
        <CredentialsForm/>
    </div>
</div>

<style>
    .app {
        display: flex;
        margin-left: 10em;
        margin-top: 15em;
    }

    .description {
        flex: 1;
        align-content: center;
    }

    .credentials-form {
        flex: 1;
        margin-top: 6em;
        justify-content: center;
        display: flex;
        place-items: start;
        text-align: center;
    }

    @media screen and (max-width: 961px) {
        .app {
            flex-direction: column-reverse;
            margin-left: 1em;
            margin-right: 1em;
            margin-top: 1em;
        }

        .credentials-form {
            margin-top: 1em;
            margin-bottom: 1em;
        }
    }
</style>