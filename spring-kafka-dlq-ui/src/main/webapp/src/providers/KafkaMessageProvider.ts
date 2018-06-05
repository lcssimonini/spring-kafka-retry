import { RESTProvider } from '@/providers/RESTProvider';

export class KafkaMessageProvider extends RESTProvider {
  protected getResource(): string {
    return '/messages';
  }

  public republish(offset: number): void {
    alert(offset)
  }
}