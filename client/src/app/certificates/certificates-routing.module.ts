import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CertificateListComponent } from './components/certificate-list/certificate-list.component';
import { CertificateFormComponent } from './components/certificate-form/certificate-form.component';
import { RoleGuardService as RoleGuard } from '../home/guards/role-guard.service';

const routes: Routes = [
  {
    path: 'certificates',
    component: CertificateListComponent,
    canActivate: [RoleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'certificates/add-new',
    component: CertificateFormComponent,
    canActivate: [RoleGuard],
    data: { roles: ['ADMIN'] }
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class CertificatesRoutingModule { }
