import { Deserializable } from './deserializable.model';

export class Log implements Deserializable {
    timestamp: string;
    message: string;
    type: string;
    sourceName: string;

    deserialize(input: any) {
        Object.assign(this, input);

        return this;
    }
}
