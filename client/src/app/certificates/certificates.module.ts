import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CertificateListComponent } from './components/certificate-list/certificate-list.component';
import { CertificateFormComponent } from './components/certificate-form/certificate-form.component';
import { CertificatesRoutingModule } from './certificates-routing.module';
import { CertificatesMaterialModule } from './certificates-material.module';
import { FlexLayoutModule } from '@angular/flex-layout';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { YearInputDialogComponent } from 'src/app/certificates/components/year-input-dialog/year-input-dialog.component';


@NgModule({
  declarations: [CertificateListComponent, CertificateFormComponent, YearInputDialogComponent],
  imports: [
    CommonModule,
    CertificatesRoutingModule,
    CertificatesMaterialModule,
    FlexLayoutModule,
    SharedModule,
    ReactiveFormsModule
  ],
  entryComponents: [YearInputDialogComponent]
})
export class CertificatesModule { }
