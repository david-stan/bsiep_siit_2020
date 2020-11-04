import { Component, OnInit } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { OrganizationService } from '../../services/organization.service';
import { Certificate } from 'src/app/models/certificate.model';
import { FormGroup, FormBuilder, FormArray, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms';
import { CertificateForm } from '../../models/certificate-form.model';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Organization } from 'src/app/models/organization.model';


function yearsValidValidator(min: number, max: number): ValidatorFn {
  return (control: AbstractControl): { [key: string]: boolean } | null => {
      if (control.value !== undefined && (isNaN(control.value) || control.value < min || control.value > max)) {
          return { 'yearsValid': true };
      }
      return null;
  };
}

@Component({
  selector: 'app-certificate-form',
  templateUrl: './certificate-form.component.html',
  styleUrls: ['./certificate-form.component.css']
})
export class CertificateFormComponent implements OnInit {

  certificateForm: FormGroup;
  organizations: Organization[];
  issuerCN: string;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private certificateService: CertificateService,
    private organizationService: OrganizationService,
    private toastr: ToastrService
  ) { }

  ngOnInit() {
    this.certificateForm = this.formBuilder.group({
      issuerCN: ["", [Validators.required, Validators.minLength(3)]],
      subjectCN: ["", [Validators.required, Validators.minLength(3)]],
      yearsValid: ["", [Validators.required, yearsValidValidator(1, 25)]],
      isCA: [false, [Validators.required]]
    });
    this.organizationService.getAll().subscribe((organizations: Organization[]) => {
      this.organizations = organizations;
    });
  }


  onFormSubmit() {
    const data = new CertificateForm();
    data.issuerCN = this.certificateForm.get('issuerCN').value;
    data.subjectCN = this.certificateForm.get('subjectCN').value;
    data.yearsValid = this.certificateForm.get('yearsValid').value;
    data.isCA = this.certificateForm.get('isCA').value;

    this.certificateService.create(data).subscribe((certificate: Certificate) => {
      this.router.navigate(['/certificates']);
    }, error => {
      let txt = '';
      if (error.error.errors != null) {
        txt = error.error.errors[0].defaultMessage;
      } else {
        txt = error.error;
      }
      
      console.log(error);
      this.toastr.error(txt, 'Certificate creation unsuccessful');
      // console.log(error.error.errors[0].defaultMessage);
    });
  }


}
