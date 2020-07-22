import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DefaultModule } from './layouts/default/default.module';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { SubmitOrderComponent } from './modules/order-create/dialogs/submit-order/submit-order.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import { MatDialogModule } from "@angular/material/dialog";
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { OrderBookedDialogComponent } from './modules/order-create/dialogs/order-booked-dialog/order-booked-dialog.component';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
//import {  MatHorizontalStepper } from '@angular/material/stepper';
import { SocketIoModule, SocketIoConfig } from 'ngx-socket-io';

import { SocketService } from './core/services/socket.service';
//const config: SocketIoConfig = { url: 'http://localhost:3000', options: {} };

//declare var require: any;

const  RestServerURL= 'http://localhost:9000';

@NgModule({
  declarations: [
    AppComponent,
    SubmitOrderComponent,
    OrderBookedDialogComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    DefaultModule,
    MatCheckboxModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    //SocketIoModule.forRoot(config),
  ],
  providers: [SocketService],
  bootstrap: [AppComponent]
})
export class AppModule { }
