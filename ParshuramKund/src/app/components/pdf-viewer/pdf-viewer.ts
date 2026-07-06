import { ChangeDetectorRef, Component } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ApplicantService } from '../../services/applicant';
import { generateClientPdf } from '../../utils/pdf-generator';

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

    // Synchronously check snapshot query parameters to prevent initial search panel layout flashing
    const idParam = this.route.snapshot.queryParams['id'];
    if (idParam) {
      this.afterRegister = false;
    }

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
  ) {
    if (typeof window !== 'undefined') {
      const navigation = this.router.getCurrentNavigation();
      if (navigation && navigation.extras && navigation.extras.state) {
        const stateApp = navigation.extras.state['applicant'];
        if (stateApp) {
          this.applicantDetails = stateApp;
          this.qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(
            `Reg ID: ${stateApp.id}\nName: ${stateApp.fullName}\nHoly Dip Date: ${stateApp.holyDipDate}\nPhone: ${stateApp.phone}`
          )}`;
        }
        const stateCo = navigation.extras.state['coTravellers'];
        if (stateCo) {
          this.coTravellersList = stateCo;
        }
      }
    }
  }

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

    // Load from sessionStorage if available to guarantee instant display
    if (typeof window !== 'undefined' && window.sessionStorage) {
      try {
        const lastRegStr = sessionStorage.getItem('lastRegistration');
        if (lastRegStr) {
          const lastReg = JSON.parse(lastRegStr);
          if (lastReg && lastReg.applicant && String(lastReg.applicant.id) === String(id)) {
            if (lastReg.applicant.rejected) {
              this.applicantDetails = null;
              this.coTravellersList = [];
              this.pdfViewBool = false;
              this.loading = false;
              this.cd.detectChanges();
              this.snackBar.open('This registration has been rejected by administration. Entry pass cannot be generated.', 'Close', {
                duration: 5000,
              });
              return;
            }
            this.applicantDetails = lastReg.applicant;
            this.coTravellersList = lastReg.coTravellers || [];
            this.qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(
              `Reg ID: ${this.applicantDetails.id}\nName: ${this.applicantDetails.fullName}\nHoly Dip Date: ${this.applicantDetails.holyDipDate}\nPhone: ${this.applicantDetails.phone}`
            )}`;
          }
        }
      } catch (e) {
        console.error('Error loading lastRegistration from sessionStorage:', e);
      }
    }

    if (!this.applicantDetails || String(this.applicantDetails.id) !== String(id)) {
      this.applicantDetails = null;
      this.coTravellersList = [];
    }
    this.cd.detectChanges();

    this.applicantService.search(id.toString()).subscribe({
      next: (results) => {
        if (results && results.length > 0) {
          const applicant = results[0];
          
          if (applicant.rejected) {
            this.applicantDetails = null;
            this.coTravellersList = [];
            this.pdfViewBool = false;
            this.rawPdfUrl = '';
            this.loading = false;
            this.cd.detectChanges();
            this.snackBar.open('This registration has been rejected by administration. Entry pass cannot be generated.', 'Close', {
              duration: 5000,
            });
            return;
          }

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

          generateClientPdf(applicant).then((doc) => {
            const blob = doc.output('blob');
            const fileURL = URL.createObjectURL(blob);
            this.rawPdfUrl = fileURL;
            this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(fileURL);
            this.pdfViewBool = true;
            this.loading = false;
            this.cd.detectChanges();
            
            this.snackBar.open('Pass Generated Locally Successfully', 'Close', {
              duration: 3000,
            });
          }).catch((pdfErr) => {
            this.loading = false;
            this.cd.detectChanges();
            console.error('Error generating PDF on client:', pdfErr);
            this.snackBar.open('Failed to generate PDF pass', 'Close', { duration: 3000 });
          });
        } else {
          this.loading = false;
          this.cd.detectChanges();
          this.snackBar.open('Registration ID not found', 'Close', { duration: 3000 });
        }
      },
      error: (err) => {
        this.loading = false;
        this.cd.detectChanges();
        console.error('Error fetching applicant details:', err);
        this.snackBar.open('Failed to retrieve pilgrim details', 'Close', { duration: 3000 });
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
        if (this.applicantDetails) {
          generateClientPdf(this.applicantDetails).then((doc) => {
            doc.autoPrint();
            doc.output('dataurlnewwindow');
          });
        }
      }
    } else {
      if (this.applicantDetails) {
        generateClientPdf(this.applicantDetails).then((doc) => {
          doc.autoPrint();
          doc.output('dataurlnewwindow');
        });
      }
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
