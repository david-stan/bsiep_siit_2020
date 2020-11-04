import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmationDialogComponent } from './components/confirmation-dialog/confirmation-dialog.component';
import { SharedMaterialModule } from './shared-material.module';
import { FlexLayoutModule } from '@angular/flex-layout';
import { ToastrModule } from 'ngx-toastr';
import { EditProfileDataDialogComponent } from './components/edit-profile-data-dialog/edit-profile-data-dialog.component';
import { FormsModule } from '@angular/forms';
import { ChangePasswordDialogComponent } from './components/change-password-dialog/change-password-dialog.component';

@NgModule({
  declarations: [ConfirmationDialogComponent, EditProfileDataDialogComponent, ChangePasswordDialogComponent, ],
  exports: [ConfirmationDialogComponent, EditProfileDataDialogComponent, ChangePasswordDialogComponent],
  imports: [
    CommonModule,
    SharedMaterialModule,
    FlexLayoutModule,
    ToastrModule.forRoot({ positionClass: 'inline' }),
    FormsModule
  ],
  entryComponents: [ConfirmationDialogComponent, EditProfileDataDialogComponent, ChangePasswordDialogComponent]
})
export class SharedModule { }
