import { ChangeDetectorRef, Component } from '@angular/core';

import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';
import { ApplicantService } from '../../services/applicant';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-test',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './test.html'
})

export class TestComponent {

  message = '';
  t: any;
  constructor(
    private http: HttpClient,
    private cd: ChangeDetectorRef,
    private applicantService: ApplicantService,


  ) {}

  submit() {

    // Prevent form refresh

    // event.preventDefault();

    // Hardcoded value

    this.applicantService.searchRegistration("2334").subscribe({
      next: (res) => {
           
        
         setTimeout(() => {

           this.t=res
        
          this.cd.detectChanges();
        
        });
           
          
        
           // this.cd.detectChanges();
         },
   
         error: () => {
   
          
   
         }
   
       });

    this.message =
      'Parshuram Kund';

    console.log(this.message);

  }

}