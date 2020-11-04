import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RoleGuardService as RoleGuard } from '../home/guards/role-guard.service';
import { LogListComponent } from './components/log-list/log-list.component';

const routes: Routes = [
  {
    path: 'logs',
    component: LogListComponent,
    canActivate: [RoleGuard],
    data: { roles: ['ADMIN', 'USER'] }
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
export class LogsRoutingModule { }
