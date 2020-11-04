import { Deserializable } from '../../models/deserializable.model';

export class CertificateForm implements Deserializable {
  issuerCN: string;
  subjectCN: string;
  yearsValid: number;
  isCA: boolean;
  

  deserialize(input: any) {
    Object.assign(this, input);
    
    return this;
  }
}
