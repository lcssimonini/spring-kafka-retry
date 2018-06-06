import { RESTProvider } from '@/providers/RESTProvider';
import axios, { AxiosResponse } from 'axios';

export class KafkaMessageProvider extends RESTProvider {
  public republish(offset: number): Promise<AxiosResponse> {
    return axios.post(`${this.getResource()}/${offset}/republish`);
  }

  protected getResource(): string {
    return '/messages';
  }
}