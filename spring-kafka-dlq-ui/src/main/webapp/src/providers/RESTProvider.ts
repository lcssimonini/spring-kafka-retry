import axios, { AxiosPromise, AxiosResponse } from 'axios';

export abstract class RESTProvider {
  protected abstract getResource(): string;

  public fetch(): Promise<AxiosResponse> {
    return axios.get(this.getResource());
  }
}