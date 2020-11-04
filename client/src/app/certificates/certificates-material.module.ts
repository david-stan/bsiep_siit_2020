import { NgModule } from '@angular/core';

import { MatInputModule, MatPaginatorModule, MatProgressSpinnerModule, MatDialogModule,
  MatSortModule, MatTableModule, MatIconModule, MatButtonModule, MatFormFieldModule,
   MatDatepickerModule, MatNativeDateModule, MatTooltipModule } from '@angular/material';
import {MatSelectModule} from '@angular/material/select';
import { FormsModule } from '@angular/forms';



@NgModule({
  imports: [
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatSortModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatDialogModule,
    MatDatepickerModule, MatNativeDateModule,
    MatSelectModule,
    FormsModule,
    MatTooltipModule
  ],
  exports: [
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatSortModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatDialogModule,
    MatDatepickerModule, MatNativeDateModule,
    MatSelectModule,
    FormsModule,
    MatTooltipModule
  ]
})
export class CertificatesMaterialModule { }
