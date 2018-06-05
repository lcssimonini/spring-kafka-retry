<template>
    <div id="app">
        <div class="row mt-4">
            <div class="col-md-4">
                <img class="d-block mb-4" src="img/logo_zup.png">
            </div>

            <div class="col-md-4">
                <h1>Kafka Dead Letter UI</h1>
            </div>
        </div>

        <KafkaDLQTable/>
    </div>
</template>
<script lang="ts" src="./Home.ts">
</script>

<style scoped lang="scss">
    #app {
        padding: 0 50px;
    }
</style>