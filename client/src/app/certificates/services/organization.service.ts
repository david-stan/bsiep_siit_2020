import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BaseService } from '../../shared/services/base.service';
import { Organization } from '../../models/organization.model';
import { HttpClient } from '@angular/common/http';

const ENDPOINTS = {
  GET_ALL: '/organizations/'
};


@Injectable({
  providedIn: 'root'
})
export class OrganizationService extends BaseService {
  organizations: Organization[] = [];

  constructor(private http: HttpClient) {
    super();
    this.setBaseUrl('pki');
  }

  getAll(): Observable<any> {
    return this.http.get(`${this.baseUrl}${ENDPOINTS.GET_ALL}`)
      .pipe(
        map((response: any) => {
          this.organizations = response.map((organization: Organization) => new Organization().deserialize(organization));
          return this.organizations;
        })
      );
  }

}
