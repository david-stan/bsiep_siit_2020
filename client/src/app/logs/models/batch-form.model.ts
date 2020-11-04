import { Deserializable } from '../../models/deserializable.model';

export class BatchForm implements Deserializable {
  batchValue: number;

  deserialize(input: any) {
    Object.assign(this, input);
    return this;
  }
}
