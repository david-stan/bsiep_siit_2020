import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog } from '@angular/material';
import { Log } from 'src/app/models/log.model';
import { LogService } from '../../services/log.service';
import { FormGroup, FormBuilder, FormArray, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms';
import { BatchForm } from '../../models/batch-form.model';

function batchValueValidator(min: number, max: number): ValidatorFn {
  return (control: AbstractControl): { [key: string]: boolean } | null => {
      if (control.value !== undefined && (isNaN(control.value) || control.value < min || control.value > max)) {
          return { 'batchValue': true };
      }
      return null;
  };
}

@Component({
  selector: 'app-log-list',
  templateUrl: './log-list.component.html',
  styleUrls: ['./log-list.component.css']
})
export class LogListComponent implements OnInit {
  logs: Log[];
  dataSource: MatTableDataSource<Log>;
  batchForm: FormGroup;
  regexForm: FormGroup;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private logService: LogService,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.batchForm = this.formBuilder.group({
      batchValue: ['', [Validators.required, batchValueValidator(0, 20000)]],
    });

    this.regexForm = this.formBuilder.group({
      regexValue: '',
    });

    this.logService.getLatestLogs().subscribe((logs: Log[]) => {
      this.initializeDataSource();
      this.logService._connect();
    });
    this.logService.newLogArrivedSource.subscribe(() => {
      this.paddedDataInit();
    });
  }

  initializeDataSource() {
    this.dataSource = new MatTableDataSource<Log>();
    this.paddedDataInit();
    // this.dataSource.data = this.logService.logs || [];
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  getDisplayedColumns(): string[] {
    return ['timestamp', 'level', 'sourceName', 'message'];
  }

  doLogTypeFilter(selectedLogType: any): void {
    this.logService.getLatestLogsFilteredByLogType(selectedLogType).subscribe(() => {
      this.paddedDataInit();
    });
  }

  setBatchValue(): void {
    const batchValue = this.batchForm.get('batchValue').value;

    this.logService.setBatchValue(batchValue).subscribe(() => {
      this.logService.setBaseUrl('siem');
    });
  }

  setRegex() : void {
    const regexValue = this.regexForm.get('regexValue').value;

    this.logService.setRegex(regexValue).subscribe(() => {
      this.logService.setBaseUrl('siem');
    })
  }

  paddedDataInit() {
    this.dataSource.data = this.logService.logs || [];
    let len = 10 - this.dataSource.data.length;
    const arr = new Array<Log>();
    while (len > 0) {
      const l = new Log();
      l.message = '';
      l.timestamp = '';
      l.type = '';
      arr.push(l);
      len--;
    }
    arr.push(...this.logService.logs)
    this.logService.logs = arr;
    this.dataSource.data = this.logService.logs || [];
  }

  colorLogLevelInTable(logType: string): string {
    switch (logType) {
      case 'ERROR':
      case 'FATAL':
        return '#e94b3cff';
      case 'INFO':
        return '#82eefd';
      case 'TRACE':
      case 'DEBUG':
        return '#3ded97';
      case 'WARN':
        return '#effd5f';
    }
  }

}
