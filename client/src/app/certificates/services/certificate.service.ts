import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { of } from 'rxjs';
import { BaseService } from '../../shared/services/base.service';
import { Certificate } from '../../models/certificate.model';
import { HttpClient } from '@angular/common/http';
import { CertificateForm } from '../models/certificate-form.model';

const ENDPOINTS = {
  GET_ALL: (pageIndex, pageSize) => `/certificates/?page=${pageIndex}&size=${pageSize}`,
  CREATE: '/certificates',
  REVOKE: (issuerCN) => `/certificates/${issuerCN}`,
  EXTEND: (issuerCN, year) => `/certificates/extendCertificate/${issuerCN}/${year}`,
  CREATE_ROOT: () => '/certificates/root'
};


@Injectable({
  providedIn: 'root'
})
export class CertificateService extends BaseService {
  certificates: Certificate[] = [];

  constructor(private http: HttpClient) {
    super();
    this.setBaseUrl('pki');
  }

  getAll(pageIndex, pageSize): Observable<any> {
    return this.http.get(`${this.baseUrl}${ENDPOINTS.GET_ALL(pageIndex, pageSize)}`)
      .pipe(
        map((response: any) => {
          this.certificates = response.map((certificate: Certificate) => new Certificate().deserialize(certificate));
          return this.certificates;
        })
      );
  }

  create(data: CertificateForm) {
    return this.http.post(`${this.baseUrl}${ENDPOINTS.CREATE}`, data).pipe(
      map((res: any) => {
        return res;
      })
    );
  }

  revoke(certificate: Certificate) {
    const issuerCN: string = certificate.x500name.rdns[0].first.value.string;
    return this.http.delete(`${this.baseUrl}${ENDPOINTS.REVOKE(issuerCN)}`).pipe(
      map((res: any) => {
        return res;
      })
    );
  }

  extend(certificate: Certificate, year: number) {
    const issuerCN: string = certificate.x500name.rdns[0].first.value.string;
    return this.http.put(`${this.baseUrl}${ENDPOINTS.EXTEND(issuerCN, year)}`, {}).pipe(
      map((res: any) => {
        return res;
      })
    );
  }

  createRoot() {
    return this.http.post(`${this.baseUrl}${ENDPOINTS.CREATE_ROOT()}`, {});
  }

}
