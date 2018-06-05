import { Component, Vue } from 'vue-property-decorator';
import KafkaDLQTable from '@/components/kafka-dlq-grid/KafkaDLQGrid';

@Component({
  components: {
    KafkaDLQTable,
  },
})
export default class Home extends Vue {
}
