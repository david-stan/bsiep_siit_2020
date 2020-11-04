import { Deserializable } from './deserializable.model';

export class User implements Deserializable {
  token: string;
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  authorities: string[];

  deserialize(input: any) {
    Object.assign(this, input);
    return this;
  }
}
