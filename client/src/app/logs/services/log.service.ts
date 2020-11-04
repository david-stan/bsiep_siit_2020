import { Injectable } from '@angular/core';
import { BaseService } from 'src/app/shared/services/base.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Log } from 'src/app/models/log.model';
import { Observable, Subject, of } from 'rxjs';
import { map } from 'rxjs/operators';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

const ENDPOINTS = {
  GET_LATEST_LOGS: () => `/getLatestLogs`,
  SET_REGEX: () => `/setRegex`,
  GET_LATEST_LOGS_FILTERED_BY_LOG_TYPE: (logType: string) => `/getLatestLogsFilteredByLogType/${logType}`,
  SET_BATCH_VALUE: (batchValue: number) => `/setBatchValue/${batchValue}`,

};

@Injectable({
  providedIn: 'root'
})
export class LogService extends BaseService {
  logs: Log[] = [];
  stompClient: any;
  webSocketEndPoint = '';
  newLogArrivedSource = new Subject<void>();

  constructor(private http: HttpClient) {
    super();
    this.setBaseUrl('siem');
    this.webSocketEndPoint = `${this.baseUrl}/wsEndPoint`;
  }

  _connect() {
    const ws = new SockJS(this.webSocketEndPoint);

    this.stompClient = Stomp.over(ws);

    this.stompClient.connect({}, () => {
      this.stompClient.subscribe('/logs', (sdkEvent: any) => {
          this.onMessageReceived(sdkEvent);
      });
    }, this.errorCallBack);
  }

  _disconnect() {
    if (this.stompClient !== null) {
        this.stompClient.disconnect();
    }
    console.log('Disconnected');
  }

  // on error, schedule a reconnection attempt
  errorCallBack(error) {
    console.log('errorCallBack -> ' + error);
  }

  onMessageReceived(message) {
    const log = new Log();
    const obj = JSON.parse(message.body);
    log.message = obj.message;
    log.timestamp = obj.timestamp;
    log.type = obj.type;
    log.sourceName = obj.sourceName;
    this.logs.shift();
    this.logs.push(log);
    this.emitLogsChange();
  }

  emitLogsChange(): void {
    this.newLogArrivedSource.next();
  }

  getLatestLogs(): Observable<any> {
    return this.http.get(`${this.baseUrl}${ENDPOINTS.GET_LATEST_LOGS()}`)
    .pipe(
      map((response: any) => {
        this.logs = response.map((log: Log) => new Log().deserialize(log));
        return this.logs;
      })
    );
  }

  getLatestLogsFilteredByLogType(logType: string): Observable<any> {
    return this.http.get(`${this.baseUrl}${ENDPOINTS.GET_LATEST_LOGS_FILTERED_BY_LOG_TYPE(logType)}`)
    .pipe(
      map((response: any) => {
        this.logs = response.map((log: Log) => new Log().deserialize(log));
        return this.logs;
      })
    );
  }

  setBatchValue(batchValue: number): Observable<any> {
    this.setBaseUrl('agent');
    
    return this.http.put(`${this.baseUrl}${ENDPOINTS.SET_BATCH_VALUE(batchValue)}`, {})
    .pipe(
      map(() => {})
    );
  }

  setRegex(regex): Observable<any> {
    return this.http.put(`${this.baseUrl}${ENDPOINTS.SET_REGEX()}`, regex)
    .pipe(
      map(() => {})
    );
  }

}
