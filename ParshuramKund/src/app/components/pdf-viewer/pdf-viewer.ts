import { ChangeDetectorRef, Component } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ApplicantService } from '../../services/applicant';

@Component({
  selector: 'app-pdf-viewer',
  imports: [MaterialModule],
  templateUrl: './pdf-viewer.html',
  styleUrl: './pdf-viewer.scss',
})
export class PdfViewer {
  afterRegister=true;
  isMobileDevice = false;
  showPdfFrame = true;
  applicantDetails: any = null;
  coTravellersList: any[] = [];

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.isMobileDevice = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
      this.showPdfFrame = !this.isMobileDevice;
    }

    this.searchQuery = '';

    this.route.queryParams.subscribe(params => {
      if(params['id']){
        this.afterRegister=false;
        this.cd.detectChanges();
        this.submitForm(params['id']);
      }
    });
  }
  registrationNo: string = '';

  loading = false;
  showPdf = false;
  pdfUrl!: SafeResourceUrl;
  rawPdf: any;
  pdfViewBool = false;
  rawPdfUrl = '';

  qrUrl = '';

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef,
    private applicantService: ApplicantService,
    private router: Router,
  ) {}

  togglePdfFrame() {
    this.showPdfFrame = !this.showPdfFrame;
    this.cd.detectChanges();
  }

  fetchApplicantDetails(id: any) {
    this.applicantService.search(id.toString()).subscribe({
      next: (results) => {
        if (results && results.length > 0) {
          const applicant = results[0];
          this.applicantDetails = applicant;
          this.qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(
            `Reg ID: ${applicant.id}\nName: ${applicant.fullName}\nHoly Dip Date: ${applicant.holyDipDate}\nPhone: ${applicant.phone}`
          )}`;
          
          if (applicant.coTraveller) {
            try {
              this.coTravellersList = JSON.parse(applicant.coTraveller);
            } catch (e) {
              console.error('Error parsing coTravellers:', e);
              this.coTravellersList = [];
            }
          }
          this.cd.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error fetching applicant details:', err);
      }
    });
  }

  submitForm(id: any) {
    this.loading = true;
    this.registrationNo = id;
    this.rawPdfUrl = '';
    this.applicantDetails = null;
    this.coTravellersList = [];
    this.cd.detectChanges();

    this.applicantService.submitForm(id).subscribe({
      next: (blob: Blob) => {
        this.loading = false;

        const fileURL = URL.createObjectURL(blob);
        this.rawPdfUrl = fileURL;

        const directUrl = `http://${window.location.hostname}:8081/api/auth/generate-pdf/${id}`;
        const finalUrl = `${directUrl}#toolbar=0&navpanes=0&scrollbar=1`;
        this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(finalUrl);

        this.fetchApplicantDetails(id);
        this.pdfViewBool = true;
        this.cd.detectChanges();

        this.snackBar.open('Pass Generated Successfully', 'Close', {
          duration: 3000,
        });
      },
      error: (err) => {
        this.loading = false;
        console.error('Error generating pass blob, using fallback:', err);
        
        const directUrl = `http://${window.location.hostname}:8081/api/auth/generate-pdf/${id}`;
        this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(`${directUrl}#toolbar=0&navpanes=0&scrollbar=1`);
        this.rawPdfUrl = directUrl;

        this.fetchApplicantDetails(id);
        this.pdfViewBool = true;
        this.cd.detectChanges();

        this.snackBar.open('Pass Rendered Successfully', 'Close', {
          duration: 3000,
        });
      }
    });
  }

  downloadPdf() {
    if (this.rawPdfUrl.startsWith('blob:')) {
      const a = document.createElement('a');
      a.href = this.rawPdfUrl;
      a.download = `${this.registrationNo}.pdf`;
      a.click();
    } else {
      window.open(this.rawPdfUrl, '_blank');
    }
  }

  printPdf() {
    const iframe = document.getElementById('pdfFrame') as HTMLIFrameElement;
    if (iframe && iframe.contentWindow) {
      try {
        iframe.contentWindow.focus();
        iframe.contentWindow.print();
      } catch (e) {
        console.error('Error executing print:', e);
        const printUrl = `http://${window.location.hostname}:8081/api/auth/generate-pdf/${this.registrationNo}`;
        window.open(printUrl, '_blank');
      }
    } else {
      const printUrl = `http://${window.location.hostname}:8081/api/auth/generate-pdf/${this.registrationNo}`;
      window.open(printUrl, '_blank');
    }
  }

  searchType: 'phone' | 'email' | 'name' | 'id' = 'phone';
  searchQuery = '';

  registrations: any[] = [];
  displayedColumns = ['registrationNo', 'name', 'visitDate','bookingDate', 'action'];
  searchSpinner: boolean = false;
  pagedRegistrations: any[] = [];
  currentPage = 1;
  pageSize = 5;
  totalPages = 0;

  // OTP Verification States
  otpSent = false;
  otpQuery = '';
  otpSending = false;
  otpVerifying = false;
  verifiedEmail = '';

  onSearchTypeChange() {
    this.searchQuery = '';
    this.registrations = [];
    this.pagedRegistrations = [];
    this.pdfViewBool = false;
    this.otpSent = false;
    this.otpQuery = '';
    this.otpSending = false;
    this.otpVerifying = false;
    this.verifiedEmail = '';
  }

  searchDirectly() {
    if (!this.searchQuery || this.searchQuery.trim().length === 0) {
      this.snackBar.open('Please enter a search term', 'Close', {
        duration: 3000,
      });
      return;
    }

    if (this.searchType === 'id') {
      const parsedId = Number(this.searchQuery.trim());
      if (isNaN(parsedId)) {
        this.snackBar.open('Please enter a valid numeric Registration ID', 'Close', {
          duration: 3000,
        });
        return;
      }
    }

    if (this.searchType === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.searchQuery.trim())) {
        this.snackBar.open('Please enter a valid email address', 'Close', {
          duration: 3000,
        });
        return;
      }
      this.triggerOtpSend(this.searchQuery.trim());
    } else {
      this.searchByQuery(this.searchQuery.trim());
    }
  }

  triggerOtpSend(email: string) {
    this.otpSending = true;
    this.cd.detectChanges();

    this.applicantService.sendOtp(email).subscribe({
      next: (res) => {
        this.otpSending = false;
        this.otpSent = true;
        this.verifiedEmail = email;
        this.otpQuery = '';
        this.cd.detectChanges();
        this.snackBar.open('OTP sent successfully to your email ID', 'Close', {
          duration: 4000,
        });
      },
      error: (err) => {
        this.otpSending = false;
        this.cd.detectChanges();
        const errMsg = err.error?.error || 'Failed to send OTP. Please try again.';
        this.snackBar.open(errMsg, 'Close', {
          duration: 4000,
        });
      }
    });
  }

  verifyOtp() {
    if (this.otpQuery.trim().length !== 6) {
      this.snackBar.open('Please enter a valid 6-digit OTP', 'Close', {
        duration: 3000,
      });
      return;
    }

    this.otpVerifying = true;
    this.cd.detectChanges();

    this.applicantService.verifyOtp(this.verifiedEmail, this.otpQuery.trim()).subscribe({
      next: (res) => {
        this.otpVerifying = false;
        this.otpSent = false;
        this.otpQuery = '';
        this.registrations = [...res];
        this.currentPage = 1;
        this.totalPages = Math.ceil(this.registrations.length / this.pageSize);
        this.updatePagedData();
        this.cd.detectChanges();

        this.snackBar.open('Verification Successful', 'Close', {
          duration: 3000,
        });

        if (res.length === 0) {
          this.snackBar.open('No registrations found for this email address', 'Close', {
            duration: 4000,
          });
        }
      },
      error: (err) => {
        this.otpVerifying = false;
        this.cd.detectChanges();
        const errMsg = err.error?.error || 'Invalid OTP. Please try again.';
        this.snackBar.open(errMsg, 'Close', {
          duration: 4000,
        });
      }
    });
  }

  cancelOtp() {
    this.otpSent = false;
    this.otpQuery = '';
    this.verifiedEmail = '';
    this.otpSending = false;
    this.otpVerifying = false;
    this.cd.detectChanges();
  }

  resendOtp() {
    if (this.verifiedEmail) {
      this.triggerOtpSend(this.verifiedEmail);
    }
  }

  searchByQuery(query: string) {
    this.searchSpinner = true;
    this.registrations = [];
    this.pagedRegistrations = [];
    this.pdfViewBool = false;

    this.applicantService.search(query).subscribe({
      next: (res) => {
        setTimeout(() => {
          this.registrations = [...res];
          this.currentPage = 1;
          this.totalPages = Math.ceil(this.registrations.length / this.pageSize);
          this.updatePagedData();
          this.searchSpinner = false;
          this.cd.detectChanges();
        });

        this.snackBar.open('Search Successful', 'Close', {
          duration: 3000,
        });

        if (res.length === 0) {
          this.snackBar.open('No Registration Found', 'Close', {
            duration: 3000,
          });
        }
      },
      error: () => {
        this.searchSpinner = false;
        this.snackBar.open('Error Fetching Data', 'Close', {
          duration: 3000,
        });
      }
    });
  }

  updatePagedData(): void {

    const startIndex =
      (this.currentPage - 1) * this.pageSize;

    const endIndex =
      startIndex + this.pageSize;

    this.pagedRegistrations =
      this.registrations.slice(startIndex, endIndex);
  }

  nextPage(): void {

    if (this.currentPage < this.totalPages) {

      this.currentPage++;

      this.updatePagedData();
    }
  }

  previousPage(): void {

    if (this.currentPage > 1) {

      this.currentPage--;

      this.updatePagedData();
    }
  }
  ngAfterViewInit() {
    this.cd.detectChanges();
  }
}
