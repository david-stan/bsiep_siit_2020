import { Component, Input, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { User } from 'src/app/models/user.model';


@Component({
  selector: 'app-edit-profile-data-dialog',
  templateUrl: './edit-profile-data-dialog.component.html',
  styleUrls: ['./edit-profile-data-dialog.component.css']
})
export class EditProfileDataDialogComponent implements OnInit {
  @Input() user: User;
  oldUser: User;
  constructor(
    public dialogRef: MatDialogRef<EditProfileDataDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public message: string) { }

    editNameInput = '';
    editUsernameInput = '';
    editEmailInput = '';

    ngOnInit() {
      this.oldUser = JSON.parse(JSON.stringify(this.user));
    }

    onCancelClick(): void {
      this.dialogRef.close();
    }
}
