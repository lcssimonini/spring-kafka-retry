import { JsonConvert } from 'json2typescript';

const mapper = new JsonConvert();

export function deserialize(json: object, clazz: any) {
  return mapper.deserializeObject(json, clazz);
}

export function deserializeArray(array: any[], clazz: any) {
  return mapper.deserializeArray(array, clazz);
}