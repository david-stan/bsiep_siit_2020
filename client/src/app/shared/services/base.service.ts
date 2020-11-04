import { Injectable } from '@angular/core';
import { config } from '../config';

@Injectable({
  providedIn: 'root'
})
export class BaseService {

  baseUrl: string;
  constructor() {
    // doesn't matter which msName is passed by default
    this.baseUrl = config.getApiUrl('pki');
  }

  setBaseUrl(msName: string) {
    this.baseUrl = config.getApiUrl(msName);
  }


}
