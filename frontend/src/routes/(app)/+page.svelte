<script lang="ts">
    import {GameAPI} from "../../lib/api/gameAPI";

    const gameApi = GameAPI.Unsafe();

    import History from "./History.svelte";
</script>

<History gameApi={gameApi}/>
