import { Component, Vue } from 'vue-property-decorator';
import { KafkaMessageProvider } from '@/providers/KafkaMessageProvider';

@Component
export default class KafkaDLQTable extends Vue {
  public columns = [
    { label: 'ID', field: 'id'},
    { label: 'Offset', field: 'offset', filterOptions: { enabled: true } },
    { label: 'Key', field: 'key', filterOptions: { enabled: true }, width: '150px' },
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
    const rows = [];

    for (const id in result.data) {
      rows.push({
        ...result.data[id],
        id
      })
    }

    this.rows = rows;
  }

  public async republishMessage(id: number) {
    try {
      await this.kafkaMessageProvider.republish(id);
      this.removeMessage(id);
      this.$notify({
        group: 'notifications',
        title: 'Success!',
        type: 'success',
        text: 'The message has been republished.',
      });

    } catch (err) {
      this.$notify({
        type: 'error',
        title: 'Error!',
        group: 'notifications',
        text: err.message || err.toString(),
      });
    }
  }

  private removeMessage(id: number): void {
    const index = this.rows.findIndex(row => row.id == id);

    this.rows.splice(index, 1);
  }
}
