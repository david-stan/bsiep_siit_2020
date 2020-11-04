import { Component, OnInit, ViewChild } from '@angular/core';
import { Certificate } from '../../../models/certificate.model';
import { CertificateService } from '../../services/certificate.service';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog } from '@angular/material';
import { ConfirmationDialogComponent } from 'src/app/shared/components/confirmation-dialog/confirmation-dialog.component';
import { Router } from '@angular/router';
import { YearInputDialogComponent } from 'src/app/certificates/components/year-input-dialog/year-input-dialog.component';

@Component({
  selector: 'app-certificate-list',
  templateUrl: './certificate-list.component.html',
  styleUrls: ['./certificate-list.component.css'],
})
export class CertificateListComponent implements OnInit {
  certificates: Certificate[];

  pageSize = 6;
  pageIndex = 0;
  length = 0;

  dataSource: MatTableDataSource<Certificate>;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(
    private certificateService: CertificateService,
    public dialog: MatDialog,
    private router: Router
  ) { }

  ngOnInit() {
    this.certificateService.getAll(this.pageIndex, this.pageSize).subscribe((certificates: Certificate[]) => {
      this.certificates = certificates;
      this.initializeDataSource();
    });
  }

  initializeDataSource() {
    this.dataSource  = new MatTableDataSource<Certificate>();
    this.dataSource.data = this.certificateService.certificates || [];
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  getDisplayedColumns(): string[] {
    return ['algorithm', 'subjectCN', 'issuerCN', 'serialNumber', 'startDate', 'endDate', 'revoke', 'extend'];
  }


  public doFilter = (value: string) => {
    this.dataSource.filter = value.trim().toLocaleLowerCase();
  }

  public pageChanged = (event: any) => {
    this.certificateService.getAll(this.pageIndex, this.pageSize).subscribe((data: any) => {
      this.certificates = data.certificates;
      this.length = data.length;
      this.pageIndex = event.pageIndex;
      this.pageSize = event.pageSize;
    });
  }

  public explodeString = (str, maxLength) => {
    var buff = "";
    var numOfLines = Math.floor(str.length/maxLength);
    for(var i = 0; i<numOfLines+1; i++) {
        buff += str.substr(i*maxLength, maxLength); if(i !== numOfLines) { buff += "\n"; }
    }
    return buff;
  }

  public getTooltip = (certificate: any) => {
    return this.explodeString(certificate.publicKey.encoded, 25);
  }

  public copyKey = (key: string) => {
    navigator.clipboard.writeText(key)
  }


  public extend = (certificate: Certificate) => {
    const modalRef = this.dialog.open(YearInputDialogComponent);
    modalRef.componentInstance.message = 'Extend for how many years? ';
    modalRef.afterClosed().subscribe(result => {
      if (!modalRef.componentInstance.year) {
        return;
      }
      const year = modalRef.componentInstance.year;
      this.certificateService.extend(certificate, year).subscribe(() => {
        this.certificateService.getAll(this.pageIndex, this.pageSize).subscribe((certificates: Certificate[]) => {
          this.certificates = certificates;
          this.initializeDataSource();
        });    
      });
    });
    
  }

  public addRoot = () => {
    return this.certificateService.createRoot().subscribe(() => {
      this.certificateService.getAll(this.pageIndex, this.pageSize).subscribe((certificates: Certificate[]) => {
        this.certificates = certificates;
        this.initializeDataSource();
      });
    });
  }

  public revoke = (certificate: Certificate) => {
    const dialogData = {
      width: '350px',
      data: 'Do you confirm the revoke of this certificate?'
    };
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, dialogData);

    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      this.certificateService.revoke(certificate).subscribe(() => {
        this.certificateService.getAll(this.pageIndex, this.pageSize).subscribe((certificates: Certificate[]) => {
          this.certificates = certificates;
          this.initializeDataSource();
        });
      })
    });
  }

}
