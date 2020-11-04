import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidatorFn} from '@angular/forms';

function yearsValidValidator(min: number, max: number): ValidatorFn {
  return (control: AbstractControl): { [key: string]: boolean } | null => {
      if (control.value !== undefined && (isNaN(control.value) || control.value < min || control.value > max)) {
          return { 'yearsValid': true };
      }
      return null;
  };
}

@Component({
  selector: 'app-year-input-dialog',
  templateUrl: './year-input-dialog.component.html',
  styleUrls: ['./year-input-dialog.component.css']
})
export class YearInputDialogComponent {
  year: number;
  yearForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<YearInputDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public message: string,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit() {
    this.yearForm = this.formBuilder.group({
      year: ["", [Validators.required, yearsValidValidator(1, 25)]]
    });
  }
  onCancelClick(): void {
    this.dialogRef.close();
  }

  onFormSubmit() {
    this.year = this.yearForm.get('year').value;
    this.dialogRef.close();
  }

}
