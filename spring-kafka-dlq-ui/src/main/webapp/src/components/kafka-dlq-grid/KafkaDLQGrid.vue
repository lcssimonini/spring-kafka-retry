<template>
    <vue-good-table
        :columns="columns"
        :rows="rows"
        :pagination-options="{
            enabled: true,
            perPage: 5,
          }"
        styleClass="vgt-table striped bordered">

        <template slot="table-row" slot-scope="props">
            <span v-if="props.column.field === 'actions'">
                  <button type="button" @click="republishMessage(props.formattedRow.offset)" class="btn btn-info">
                      <i class="fas fa-sync-alt"></i>
                  </button>
            </span>
            <span v-else>
                  {{props.formattedRow[props.column.field]}}
            </span>
        </template>

    </vue-good-table>
</template>

<script lang="ts" src="./KafkaDLQGrid.ts">
</script>
