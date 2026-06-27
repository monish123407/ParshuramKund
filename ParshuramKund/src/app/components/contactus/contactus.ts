import { Component } from '@angular/core';

import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../material.module';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApplicantService } from '../../services/applicant';

@Component({
  selector: 'app-contactus',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  templateUrl: './contactus.html',
  styleUrl: './contactus.scss',
})
export class Contactus {
  feedback = {
    name: '',
    email: '',
    phone: '',
    subject: '',
    message: ''
  };
  isSubmitting = false;

  constructor(
    private snackBar: MatSnackBar,
    private applicantService: ApplicantService
  ) {}

  submitFeedback() {
    if (this.isSubmitting) return;
    if (!this.feedback.name || !this.feedback.email || !this.feedback.message) {
      this.snackBar.open('Please fill in all required fields.', 'Close', { duration: 3000 });
      return;
    }
    
    this.isSubmitting = true;
    this.applicantService.sendInquiry(this.feedback).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open('Thank you for contacting us! Your inquiry has been submitted.', 'Close', { duration: 5000 });
        this.feedback = {
          name: '',
          email: '',
          phone: '',
          subject: '',
          message: ''
        };
      },
      error: (err) => {
        this.isSubmitting = false;
        this.snackBar.open('Failed to submit inquiry. Please try again later.', 'Close', { duration: 3000 });
        console.error('Submit inquiry error: ', err);
      }
    });
  }
}
