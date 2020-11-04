import { Deserializable } from './deserializable.model';

export class Organization implements Deserializable {
  

  deserialize(input: any) {
    Object.assign(this, input);
    
    return this;
  }
}
