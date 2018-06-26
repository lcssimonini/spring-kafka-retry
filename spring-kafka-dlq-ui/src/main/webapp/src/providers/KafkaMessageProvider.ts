import { RESTProvider } from '@/providers/RESTProvider';
import axios, { AxiosResponse } from 'axios';

export class KafkaMessageProvider extends RESTProvider {
  public republish(id: number): Promise<AxiosResponse> {
    return axios.post(`${this.getResource()}/${id}/republish`, {});
  }

  protected getResource(): string {
    return '/messages';
  }
}