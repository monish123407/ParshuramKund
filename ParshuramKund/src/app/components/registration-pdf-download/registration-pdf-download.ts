import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApplicantService } from '../../services/applicant';
import { generateClientPdf } from '../../utils/pdf-generator';

@Component({
  selector: 'app-registration-pdf-download',
  imports: [MaterialModule, CommonModule, RouterModule],
  templateUrl: './registration-pdf-download.html',
  styleUrl: './registration-pdf-download.scss',
})
export class RegistrationPdfDownload implements OnInit {
  registrationData: any = {};
  applicantDetails: any = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef,
    private snackBar: MatSnackBar,
    private applicantService: ApplicantService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.loading = true;
        this.applicantService.search(id).subscribe({
          next: (results) => {
            this.loading = false;
            if (results && results.length > 0) {
              const applicant = results[0];
              this.applicantDetails = applicant;
              this.registrationData = {
                id: applicant.id,
                name: applicant.fullName,
                mobileNo: applicant.phone,
                email: applicant.email,
                gender: applicant.gender,
                address: `${applicant.presentAddress}, ${applicant.presentDistrict}, ${applicant.presentState}`
              };
              this.cd.detectChanges();
            }
          },
          error: (err) => {
            this.loading = false;
            console.error('Error fetching registration details:', err);
            this.snackBar.open('Failed to load registration details', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  async downloadForm() {
    if (!this.applicantDetails) {
      this.snackBar.open('Registration details not loaded yet', 'Close', { duration: 3000 });
      return;
    }

    if (this.applicantDetails.rejected) {
      this.snackBar.open('This registration has been rejected. Pass cannot be generated.', 'Close', { duration: 3000 });
      return;
    }
    
    this.snackBar.open('Generating your entry pass...', 'Close', { duration: 1500 });
    
    try {
      const doc = await generateClientPdf(this.applicantDetails);
      doc.save(`ParshuramPass_${this.registrationData.id}.pdf`);
      this.snackBar.open('Pass downloaded successfully!', 'Close', { duration: 3000 });
    } catch (e) {
      console.error('Failed to generate PDF:', e);
      this.snackBar.open('Failed to generate PDF pass. Please try again.', 'Close', { duration: 4000 });
    }
  }
}
