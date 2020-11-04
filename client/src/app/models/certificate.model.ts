import { Deserializable } from './deserializable.model';

export class Certificate implements Deserializable {
  publicKey: object;
  x500name: any;
  serialNumber: string;
  startDate: Date;
  endDate: Date;
  

  deserialize(input: any) {
    Object.assign(this, input);
    this.startDate = new Date(input.startDate);
    this.endDate = new Date(input.endDate);
    
    return this;
  }
}
