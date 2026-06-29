import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../material.module';
import { ApplicantService } from '../../services/applicant';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin implements OnInit {
  isLoggedIn = false;
  username = '';
  password = '';
  currentUser: any = null;

  registrations: any[] = [];
  inquiries: any[] = [];
  members: any[] = [];
  activeTab: 'registrations' | 'inquiries' | 'members' = 'registrations';
  searchQuery = '';
  selectedGender = '';
  selectedVisitDate = '';
  uniqueVisitDates: string[] = [];
  
  loading = false;
  selectedRegistration: any = null;
  selectedInquiry: any = null;
  showDisposalModal = false;
  disposalMessage = '';
  inquiryToDispose: any = null;
  isDisposing = false;
  regCurrentPage = 1;
  regPageSize = 10;

  newMember = { username: '', password: '', fullName: '', role: 'MELA_OPERATOR' };
  selectedMemberToEdit: any = null;
  showEditMemberModal = false;

  stats = {
    total: 0,
    coApplicants: 0,
    totalDevotees: 0,
    male: 0,
    female: 0,
    inquiries: 0
  };


  userEnteredCaptcha = '';
  captchaCode = '';

  constructor(
    private applicantService: ApplicantService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Check if session was already logged in
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const auth = sessionStorage.getItem('admin_auth');
      if (auth === 'true') {
        this.isLoggedIn = true;
        this.currentUser = {
          username: sessionStorage.getItem('admin_username'),
          role: sessionStorage.getItem('admin_role'),
          fullName: sessionStorage.getItem('admin_fullname')
        };
        this.loadData();
      } else {
        setTimeout(() => this.generateCaptcha(), 150);
      }
    }
  }

  generateCaptcha() {
    if (typeof document === 'undefined') return;
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    this.captchaCode = '';
    for (let i = 0; i < 6; i++) {
      this.captchaCode += chars.charAt(Math.floor(Math.random() * chars.length));
    }

    const canvas = document.getElementById('captchaCanvas') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Clear background
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#f3f4f6';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Draw some noise lines
    ctx.strokeStyle = '#cbd5e1';
    for (let i = 0; i < 6; i++) {
      ctx.beginPath();
      ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height);
      ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height);
      ctx.lineWidth = 1 + Math.random() * 1.5;
      ctx.stroke();
    }

    // Draw CAPTCHA characters
    const colors = ['#e11d48', '#2563eb', '#16a34a', '#d97706', '#7c3aed', '#0891b2'];
    ctx.font = 'bold 24px Courier New';
    ctx.textBaseline = 'middle';

    for (let i = 0; i < this.captchaCode.length; i++) {
      const char = this.captchaCode[i];
      ctx.save();
      
      // Calculate random horizontal spacing, slightly offset vertical positioning, and rotation
      const x = 15 + i * 20 + Math.random() * 4;
      const y = canvas.height / 2 + (Math.random() * 8 - 4);
      const angle = (Math.random() * 24 - 12) * Math.PI / 180; // +/- 12 degrees

      ctx.translate(x, y);
      ctx.rotate(angle);
      
      ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)];
      ctx.fillText(char, 0, 0);
      ctx.restore();
    }
  }

  login() {
    if (!this.userEnteredCaptcha) {
      this.snackBar.open('Please enter the CAPTCHA code', 'Close', { duration: 3000 });
      return;
    }

    if (this.userEnteredCaptcha.toLowerCase() !== this.captchaCode.toLowerCase()) {
      this.snackBar.open('Invalid CAPTCHA code. Please try again.', 'Close', { duration: 3000 });
      this.userEnteredCaptcha = '';
      this.generateCaptcha();
      return;
    }

    this.applicantService.adminLogin({ username: this.username, password: this.password }).subscribe({
      next: (res: any) => {
        this.isLoggedIn = true;
        this.currentUser = res;
        if (typeof window !== 'undefined' && window.sessionStorage) {
          sessionStorage.setItem('admin_auth', 'true');
          sessionStorage.setItem('admin_username', res.username);
          sessionStorage.setItem('admin_role', res.role);
          sessionStorage.setItem('admin_fullname', res.fullName);
        }
        this.snackBar.open(`Welcome back, ${res.fullName}!`, 'Close', { duration: 3000 });
        this.cdr.detectChanges();
        this.loadData();
      },
      error: (err) => {
        this.snackBar.open('Invalid username or password. Hint: admin / admin123', 'Close', { duration: 3000 });
        this.userEnteredCaptcha = '';
        this.generateCaptcha();
      }
    });
  }

  logout() {
    this.isLoggedIn = false;
    this.currentUser = null;
    this.members = [];
    this.activeTab = 'registrations';
    if (typeof window !== 'undefined' && window.sessionStorage) {
      sessionStorage.removeItem('admin_auth');
      sessionStorage.removeItem('admin_username');
      sessionStorage.removeItem('admin_role');
      sessionStorage.removeItem('admin_fullname');
    }
    this.registrations = [];
    this.cdr.detectChanges();
    setTimeout(() => this.generateCaptcha(), 150);
    this.snackBar.open('Logged out successfully', 'Close', { duration: 3000 });
  }

  loadData() {
    this.loading = true;
    this.cdr.detectChanges();
    this.applicantService.getAllRegistrations().subscribe({
      next: (res) => {
        this.registrations = res;
        this.applicantService.getAllInquiries().subscribe({
          next: (inqRes) => {
            this.inquiries = inqRes;
            this.calculateStats();
            this.extractUniqueDates();
            if (this.currentUser?.role === 'SUPER_ADMIN') {
              this.loadMembers();
            } else {
              this.loading = false;
              this.cdr.detectChanges();
            }
          },
          error: (inqErr) => {
            console.error('Load inquiries error: ', inqErr);
            this.calculateStats();
            this.extractUniqueDates();
            if (this.currentUser?.role === 'SUPER_ADMIN') {
              this.loadMembers();
            } else {
              this.loading = false;
              this.cdr.detectChanges();
            }
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.cdr.detectChanges();
        this.snackBar.open('Error loading registrations from backend', 'Close', { duration: 3000 });
        console.error('Load registrations error: ', err);
      }
    });
  }

  extractUniqueDates() {
    const dates = this.registrations.map(r => r.holyDipDate).filter(Boolean);
    this.uniqueVisitDates = Array.from(new Set(dates));
  }

  calculateStats() {
    this.stats.total = this.registrations.length;
    this.stats.coApplicants = this.registrations.reduce((acc, r) => {
      if (r.coApplicant) {
        try {
          const arr = JSON.parse(r.coApplicant);
          return acc + (Array.isArray(arr) ? arr.length : 0);
        } catch (e) {
          return acc;
        }
      }
      return acc;
    }, 0);

    let maleCount = 0;
    let femaleCount = 0;

    for (const r of this.registrations) {
      if (r.gender?.toLowerCase() === 'male') {
        maleCount++;
      } else if (r.gender?.toLowerCase() === 'female') {
        femaleCount++;
      }

      if (r.coApplicant) {
        try {
          const arr = JSON.parse(r.coApplicant);
          if (Array.isArray(arr)) {
            for (const co of arr) {
              if (co.gender?.toLowerCase() === 'male') {
                maleCount++;
              } else if (co.gender?.toLowerCase() === 'female') {
                femaleCount++;
              }
            }
          }
        } catch (e) {
          // Ignore json parsing error
        }
      }
    }

    this.stats.male = maleCount;
    this.stats.female = femaleCount;
    this.stats.totalDevotees = maleCount + femaleCount;
    this.stats.inquiries = this.inquiries.length;
  }

  get filteredRegistrations() {
    return this.registrations.filter(r => {
      const matchesSearch = !this.searchQuery ||
        r.fullName?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        r.phone?.includes(this.searchQuery) ||
        r.id?.toString().includes(this.searchQuery) ||
        r.email?.toLowerCase().includes(this.searchQuery.toLowerCase());
      
      const matchesGender = !this.selectedGender || r.gender?.toLowerCase() === this.selectedGender.toLowerCase();
      const matchesVisitDate = !this.selectedVisitDate || r.holyDipDate === this.selectedVisitDate;
      
      return matchesSearch && matchesGender && matchesVisitDate;
    });
  }

  get regTotalPages() {
    return Math.max(1, Math.ceil(this.filteredRegistrations.length / this.regPageSize));
  }

  get pagedRegistrations() {
    const totalPages = this.regTotalPages;
    if (this.regCurrentPage > totalPages) {
      this.regCurrentPage = 1;
    }
    const startIndex = (this.regCurrentPage - 1) * this.regPageSize;
    return this.filteredRegistrations.slice(startIndex, startIndex + this.regPageSize);
  }



  viewDetails(reg: any) {
    this.selectedRegistration = reg;
  }

  closeDetails() {
    this.selectedRegistration = null;
  }

  getCoApplicantsList(coApplicantJson: string): any[] {
    if (!coApplicantJson) return [];
    try {
      const parsed = JSON.parse(coApplicantJson);
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }

  downloadPass(id: number) {
    const role = this.currentUser?.role || '';
    window.open(`http://${window.location.hostname}:8081/api/auth/generate-pdf/${id}?role=${role}`, '_blank');
  }

  getAadharPhotoUrl(id: any): string {
    if (typeof window !== 'undefined') {
      return `http://${window.location.hostname}:8081/api/auth/aadhar-photo/${id}`;
    }
    return `http://localhost:8081/api/auth/aadhar-photo/${id}`;
  }

  deleteBooking(id: number) {
    if (confirm(`Are you sure you want to cancel Registration ID: ${id}? This action is permanent.`)) {
      this.applicantService.deleteRegistration(id).subscribe({
        next: () => {
          this.snackBar.open(`Registration #${id} successfully cancelled.`, 'Close', { duration: 3000 });
          if (this.selectedRegistration?.id === id) {
            this.selectedRegistration = null;
          }
          this.loadData();
        },
        error: () => {
          this.snackBar.open('Error cancelling registration', 'Close', { duration: 3000 });
        }
      });
    }
  }

  resendEmail(id: number) {
    this.snackBar.open(`Initiating pass email resend for Registration #${id}...`, 'Close', { duration: 2000 });
    this.applicantService.resendRegistrationEmail(id).subscribe({
      next: (res: any) => {
        this.snackBar.open(`Pass email successfully queued for Registration #${id}.`, 'Close', { duration: 3000 });
      },
      error: (err: any) => {
        this.snackBar.open(err.error?.error || 'Failed to resend pass email', 'Close', { duration: 3000 });
      }
    });
  }

  exportToCSV() {
    const data = this.filteredRegistrations;
    if (data.length === 0) {
      this.snackBar.open('No data available to export', 'Close', { duration: 3000 });
      return;
    }
    const headers = [
      'ID',
      'Full Name',
      'Age',
      'Gender',
      'Phone',
      'Email',
      'Aadhaar Number',
      'Aadhaar Photo Path',
      'Comorbidities',
      'Holy Dip Date',
      'Booking Date',
      'Present Address',
      'Present State',
      'Present District',
      'Present Pin Code',
      'Permanent Address',
      'Permanent State',
      'Permanent District',
      'Permanent Pin Code',
      'Is Co-Applicant Present',
      'Co-Applicants Data'
    ];
    const escapeCSV = (val: any) => {
      if (val === undefined || val === null) return '""';
      const str = String(val);
      return `"${str.replace(/"/g, '""')}"`;
    };
    const rows = data.map(r => [
      r.id,
      escapeCSV(r.fullName),
      r.age,
      escapeCSV(r.gender),
      escapeCSV(r.phone),
      escapeCSV(r.email),
      escapeCSV(r.aadharNumber),
      escapeCSV(r.aadharPhotoPath),
      escapeCSV(r.comorbidities),
      escapeCSV(r.holyDipDate),
      escapeCSV(r.bookingDate),
      escapeCSV(r.presentAddress),
      escapeCSV(r.presentState),
      escapeCSV(r.presentDistrict),
      escapeCSV(r.presentPinCode),
      escapeCSV(r.permanentAddress),
      escapeCSV(r.permanentState),
      escapeCSV(r.permanentDistrict),
      escapeCSV(r.permanentPinCode),
      (r.isPresentCoApplicant === true || r.isPresentCoApplicant === 'true') ? 'Yes' : 'No',
      escapeCSV(r.coApplicant)
    ]);
    const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `parshuram_kund_registrations_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    this.snackBar.open('CSV Exported successfully', 'Close', { duration: 3000 });
  }

  get filteredInquiries() {
    return this.inquiries.filter(i => {
      const query = this.searchQuery ? this.searchQuery.toLowerCase() : '';
      if (!query) return true;
      return (i.name && i.name.toLowerCase().includes(query)) ||
             (i.email && i.email.toLowerCase().includes(query)) ||
             (i.phone && i.phone.includes(query)) ||
             (i.subject && i.subject.toLowerCase().includes(query)) ||
             (i.message && i.message.toLowerCase().includes(query)) ||
             (i.id && i.id.toString().includes(query));
    });
  }

  viewInquiryDetails(inq: any) {
    this.selectedInquiry = inq;
  }

  closeInquiryDetails() {
    this.selectedInquiry = null;
  }

  disposeInquiry(id: number) {
    const inq = this.inquiries.find(i => i.id === id);
    if (inq) {
      this.inquiryToDispose = inq;
      this.disposalMessage = '';
      this.showDisposalModal = true;
    }
  }

  cancelDisposal() {
    if (this.isDisposing) return;
    this.showDisposalModal = false;
    this.inquiryToDispose = null;
    this.disposalMessage = '';
  }

  confirmDisposal() {
    if (!this.inquiryToDispose || !this.disposalMessage.trim() || this.isDisposing) return;
    const id = this.inquiryToDispose.id;
    this.isDisposing = true;
    this.applicantService.disposeInquiryWithMessage(id, this.disposalMessage).subscribe({
      next: () => {
        this.isDisposing = false;
        this.snackBar.open(`Inquiry #${id} successfully disposed of and response email sent.`, 'Close', { duration: 3000 });
        if (this.selectedInquiry?.id === id) {
          this.selectedInquiry = null;
        }
        this.showDisposalModal = false;
        this.inquiryToDispose = null;
        this.disposalMessage = '';
        this.loadData();
      },
      error: (err) => {
        this.isDisposing = false;
        this.snackBar.open('Error disposing of inquiry', 'Close', { duration: 3000 });
        console.error('Dispose inquiry error: ', err);
      }
    });
  }

  loadMembers() {
    this.applicantService.getAdminMembers().subscribe({
      next: (res) => {
        this.members = res;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.cdr.detectChanges();
        console.error('Error loading members:', err);
      }
    });
  }

  addMember() {
    if (!this.newMember.username || !this.newMember.password || !this.newMember.fullName) {
      this.snackBar.open('Please fill all fields', 'Close', { duration: 3000 });
      return;
    }
    this.applicantService.addAdminMember(this.newMember).subscribe({
      next: () => {
        this.snackBar.open('Member added successfully', 'Close', { duration: 3000 });
        this.newMember = { username: '', password: '', fullName: '', role: 'MELA_OPERATOR' };
        this.loadMembers();
      },
      error: (err) => {
        this.snackBar.open(err.error?.error || 'Failed to add member', 'Close', { duration: 3000 });
      }
    });
  }

  editMember(member: any) {
    this.selectedMemberToEdit = { ...member, password: '' };
    this.showEditMemberModal = true;
  }

  confirmEditMember() {
    if (!this.selectedMemberToEdit.username || !this.selectedMemberToEdit.fullName) {
      this.snackBar.open('Please fill required fields', 'Close', { duration: 3000 });
      return;
    }
    const id = this.selectedMemberToEdit.id;
    this.applicantService.updateAdminMember(id, this.selectedMemberToEdit).subscribe({
      next: () => {
        this.snackBar.open('Member updated successfully', 'Close', { duration: 3000 });
        this.showEditMemberModal = false;
        this.selectedMemberToEdit = null;
        this.loadMembers();
      },
      error: (err) => {
        this.snackBar.open(err.error?.error || 'Failed to update member', 'Close', { duration: 3000 });
      }
    });
  }

  cancelEditMember() {
    this.showEditMemberModal = false;
    this.selectedMemberToEdit = null;
  }

  deleteAdminMember(id: number) {
    const memberName = this.members.find(m => m.id === id)?.username;
    if (this.currentUser?.username === memberName) {
      this.snackBar.open('You cannot delete yourself!', 'Close', { duration: 3000 });
      return;
    }
    if (confirm('Are you sure you want to delete this admin member?')) {
      this.applicantService.deleteAdminMember(id).subscribe({
        next: () => {
          this.snackBar.open('Member deleted successfully', 'Close', { duration: 3000 });
          this.loadMembers();
        },
        error: (err) => {
          this.snackBar.open('Failed to delete member', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
