import { Component, Vue } from 'vue-property-decorator';
import { KafkaMessageProvider } from '@/providers/KafkaMessageProvider';
import { KafkaMessage } from '@/model/KafkaMessage';

@Component
export default class KafkaDLQTable extends Vue {
  public columns = [
    { label: 'Offset', field: 'offset', filterOptions: { enabled: true } },
    { label: 'Key', field: 'key', type: 'number', filterOptions: { enabled: true }, width: '150px' },
    { label: 'Header', field: 'header', filterOptions: { enabled: true } },
    { label: 'Payload', field: 'payload', filterOptions: { enabled: true } },
    { label: 'Timestamp', field: 'timestamp', type: 'number', filterOptions: { enabled: true } },
    { label: 'Actions', field: 'actions', tdClass: 'text-center', width: '50px' },
  ];
  public rows: any[] = [];
  private kafkaMessageProvider: KafkaMessageProvider;

  constructor() {
    super();
    this.kafkaMessageProvider = new KafkaMessageProvider();
  }

  public async mounted() {
    const result = await this.kafkaMessageProvider.fetch();

    this.rows = result.data;
  }

  public republishMessage(offset: number) {
    this.kafkaMessageProvider.republish(offset);
  }
}
