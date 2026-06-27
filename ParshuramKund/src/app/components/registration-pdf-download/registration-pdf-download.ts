import { ChangeDetectorRef, Component } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer } from '@angular/platform-browser';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { ApplicantService } from '../../services/applicant';

@Component({
  selector: 'app-registration-pdf-download',
  imports: [MaterialModule],
  templateUrl: './registration-pdf-download.html',
  styleUrl: './registration-pdf-download.scss',
})
export class RegistrationPdfDownload {
  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef,
    private applicantService: ApplicantService

  ) {}
  registrationData: any;



  ngOnInit(): void {

   
    this.route.queryParams.subscribe(params => {

      
      this.registrationData = {
        id: params['id'],
        name: params['name'],
        mobileNo: params['mobileNo'],
        email: params['email'],
        gender: params['gender'],
        address: params['address']
      };
      console.log(this.registrationData);
      
    
  
    });
    // fallback if refresh happens
    if (!this.registrationData) {
      const saved = localStorage.getItem('registrationData');
      if (saved) {
        this.registrationData = JSON.parse(saved);
      }
    }
  }

  downloadForm(): void {

    // backend pdf api
    const pdfUrl = `http://${window.location.hostname}:8081/api/auth/generate-pdf/${this.registrationData.id}`;

    window.open(pdfUrl, '_blank');
  }

}
