<script lang="ts">

</script>

<div class="modal">
    <div class="modal-content">
        <slot/>
    </div>
</div>

<style>
    .modal {
        display: block;
        position: fixed;
        z-index: 1;
        padding-top: 30vh;
        padding-right: 30vw;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        overflow: auto;
        background-color: rgba(0,0,0,0.5);
    }

    .modal-content {
        display: flex;
        background: linear-gradient(175deg,red,orange,yellow,lawngreen,turquoise,
        #003cff,blueviolet,blueviolet);
        background-size: 400% 100%;
        animation: gradient 15s ease infinite;
        color: #1a1a1a;
        font-size: 3em;
        margin: auto;
        padding: 20px;
        border-radius: 16px;
        width: 70%;
    }

    @keyframes gradient {
        0% {
            background-position: 0 50%;
        }
        50% {
            background-position: 100% 50%;
        }
        100% {
            background-position: 0 50%;
        }
    }
</style>