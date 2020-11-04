import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../shared/shared.module';
import { LogListComponent } from './components/log-list/log-list.component';
import { LogsMaterialModule } from './logs-material.module';
import { LogsRoutingModule } from './logs-routing.module';
import { ReactiveFormsModule } from '@angular/forms';



@NgModule({
  declarations: [LogListComponent],
  imports: [
    CommonModule,
    SharedModule,
    LogsMaterialModule,
    LogsRoutingModule,
    ReactiveFormsModule
  ]
})
export class LogsModule { }
