import { JsonObject } from 'json2typescript';

@JsonObject
export class KafkaMessage {
  public offset?: number;
  public key?: string;
  public header?: string;
  public payload?: string;
  public timestamp?: number;
}