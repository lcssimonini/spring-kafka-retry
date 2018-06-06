import axios, { AxiosResponse} from 'axios';

export abstract class RESTProvider {
  public fetch(): Promise<AxiosResponse> {
    return axios.get(this.getResource());
  }

  protected abstract getResource(): string;
}