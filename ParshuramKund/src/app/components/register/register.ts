import { ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, inject } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { ApplicantService } from '../../services/applicant';
import { HttpClient } from '@angular/common/http';
import { STATES, STATE_DISTRICTS_MAP } from './states-and-districts';

@Component({
  selector: 'app-register',
  imports: [
    CommonModule, 
    RouterModule,
    MaterialModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register implements OnInit, OnDestroy {
  statesList = STATES;
  presentDistricts: string[] = [];
  permanentDistricts: string[] = [];
  customPresentDistrict = '';
  customPermanentDistrict = '';

  filteredPresentStates: string[] = [];
  filteredPermanentStates: string[] = [];
  filteredPresentDistricts: string[] = [];
  filteredPermanentDistricts: string[] = [];

  currentStep = 1;
  isSubmitting = false;
  isRegistrationSuccess = false;
  showIlpPopup = false;

  constructor(
    private http: HttpClient, 
    private applicantService: ApplicantService, 
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}
  ngOnInit() {
    this.filteredPresentStates = [...this.statesList];
    this.filteredPermanentStates = [...this.statesList];
    this.showIlpPopup = true;
    this.cd.detectChanges();
  }
  closeIlpPopup() {
    this.showIlpPopup = false;
  }
  selectedAadharFile: File | null = null;
  aadharFileError: string | null = null;
  isUploadingAadhar = false;
  coTravellers: any[] = [];
  applicant = {
    fullName: '',
    holyDipDate: '',
    age: '',
    gender: '',
    email: '',
    phone: '',
    comorbidities: [] as string[],
    presentAddress: '',
    presentState: '',
    presentDistrict: '',
    presentPinCode:'',
    permanentAddress: '',
    permanentState: '',
    permanentDistrict: '',
    permanentPinCode:'',
    sameAddress: false,
    coTraveller: '',
    declaration: false,
    isPresentCoApplicant:'No',
    aadharNumber: '',
    aadharPhotoPath: ''
  };

  onAadharFileSelected(event: any) {
    const files = event.target.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (file.size > 200 * 1024) {
        this.aadharFileError = 'File size must be under 200KB';
        this.selectedAadharFile = null;
        this.applicant.aadharPhotoPath = '';
        return;
      }
      const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
      if (!allowedTypes.includes(file.type)) {
        this.aadharFileError = 'Only Jpeg, Png, and Pdf files are allowed';
        this.selectedAadharFile = null;
        this.applicant.aadharPhotoPath = '';
        return;
      }
      this.aadharFileError = null;
      this.selectedAadharFile = file;

      const previousPath = this.applicant.aadharPhotoPath;
      if (previousPath && previousPath.trim() !== '') {
        this.applicantService.deleteAadharPhoto(previousPath).subscribe({
          next: () => console.log('Cleaned up previous Aadhaar photo:', previousPath),
          error: (err) => console.error('Failed to cleanup previous photo:', err)
        });
      }

      this.isUploadingAadhar = true;
      this.applicant.aadharPhotoPath = '';
      this.cd.detectChanges();

      this.applicantService.uploadAadharPhoto(file).subscribe({
        next: (uploadRes: any) => {
          this.isUploadingAadhar = false;
          this.applicant.aadharPhotoPath = uploadRes.filePath;
          this.cd.detectChanges();
        },
        error: (err) => {
          this.isUploadingAadhar = false;
          this.selectedAadharFile = null;
          this.applicant.aadharPhotoPath = '';
          this.aadharFileError = 'Failed to upload Aadhaar document. Please try again.';
          this.cd.detectChanges();
        }
      });
    }
  }

  isStep1Valid(): boolean {
    const app = this.applicant;
    const aadharRegex = /^\d{12}$/;
    return !!(
      app.fullName && app.fullName.trim() !== '' &&
      app.holyDipDate && app.holyDipDate !== '' &&
      app.age !== null && app.age !== undefined && app.age !== '' &&
      app.aadharNumber && aadharRegex.test(app.aadharNumber) &&
      this.selectedAadharFile !== null &&
      app.aadharPhotoPath && app.aadharPhotoPath !== '' &&
      !this.isUploadingAadhar
    );
  }

  ngOnDestroy() {
    this.cleanupAadharPhoto();
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any) {
    this.cleanupAadharPhoto();
  }

  private cleanupAadharPhoto() {
    if (this.applicant.aadharPhotoPath && !this.isRegistrationSuccess) {
      const url = `${this.applicantService.apiUrl}/delete-aadhar`;
      const payload = JSON.stringify({ filePath: this.applicant.aadharPhotoPath });
      
      if (typeof navigator !== 'undefined' && navigator.sendBeacon) {
        const blob = new Blob([payload], { type: 'application/json' });
        navigator.sendBeacon(url, blob);
      } else {
        this.applicantService.deleteAadharPhoto(this.applicant.aadharPhotoPath).subscribe();
      }
    }
  }


  onComorbiditiesChange(selected: string[]) {
    if (selected.includes('None') && selected.length > 1) {
      const lastSelected = selected[selected.length - 1];
      if (lastSelected === 'None') {
        this.applicant.comorbidities = ['None'];
      } else {
        this.applicant.comorbidities = selected.filter(val => val !== 'None');
      }
    }
  }

  onCoTravellerComorbiditiesChange(traveller: any, selected: string[]) {
    if (selected.includes('None') && selected.length > 1) {
      const lastSelected = selected[selected.length - 1];
      if (lastSelected === 'None') {
        traveller.comorbidities = ['None'];
      } else {
        traveller.comorbidities = selected.filter(val => val !== 'None');
      }
    }
  }

  isStep2Valid(): boolean {
    const app = this.applicant;
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const phoneRegex = /^[0-9]{10}$/;
    return !!(
      app.phone && phoneRegex.test(app.phone.trim()) &&
      app.gender && app.gender !== '' &&
      app.email && emailRegex.test(app.email.trim())
    );
  }

  isStep3Valid(): boolean {
    const app = this.applicant;
    const isValidPresentState = this.statesList.includes(app.presentState);
    const isValidPermanentState = this.statesList.includes(app.permanentState);
    const isValidPresentDistrict = this.presentDistricts.includes(app.presentDistrict) || 
      (app.presentDistrict === 'Other' && this.customPresentDistrict && this.customPresentDistrict.trim() !== '');
    const isValidPermanentDistrict = this.permanentDistricts.includes(app.permanentDistrict) || 
      (app.permanentDistrict === 'Other' && this.customPermanentDistrict && this.customPermanentDistrict.trim() !== '');

    return !!(
      app.presentAddress && app.presentAddress.trim() !== '' &&
      isValidPresentState &&
      isValidPresentDistrict &&
      app.presentPinCode && app.presentPinCode.trim() !== '' &&
      app.permanentAddress && app.permanentAddress.trim() !== '' &&
      isValidPermanentState &&
      isValidPermanentDistrict &&
      app.permanentPinCode && app.permanentPinCode.trim() !== ''
    );
  }

  isStep4Valid(): boolean {
    const app = this.applicant;
    if (app.isPresentCoApplicant === 'Yes') {
      if (this.coTravellers.length === 0) {
        return false;
      }
      for (const t of this.coTravellers) {
        if (!t.name || t.name.trim() === '' || !t.age || !t.gender) {
          return false;
        }
      }
    }
    return true;
  }

  isStep5Valid(): boolean {
    return this.applicant.declaration === true &&
      this.isStep1Valid() &&
      this.isStep2Valid() &&
      this.isStep3Valid() &&
      this.isStep4Valid();
  }

  get isFormInvalid(): boolean {
    return !this.isStep5Valid();
  }

  coApplicant={
    name:'',
    age:'',
    gender:''
  }

 

  filterPresentStates() {
    const val = this.applicant.presentState || '';
    this.filteredPresentStates = this.statesList.filter(s =>
      s.toLowerCase().includes(val.toLowerCase())
    );
  }

  filterPermanentStates() {
    const val = this.applicant.permanentState || '';
    this.filteredPermanentStates = this.statesList.filter(s =>
      s.toLowerCase().includes(val.toLowerCase())
    );
  }

  filterPresentDistricts() {
    const val = this.applicant.presentDistrict || '';
    this.filteredPresentDistricts = this.presentDistricts.filter(d =>
      d.toLowerCase().includes(val.toLowerCase())
    );
  }

  filterPermanentDistricts() {
    const val = this.applicant.permanentDistrict || '';
    this.filteredPermanentDistricts = this.permanentDistricts.filter(d =>
      d.toLowerCase().includes(val.toLowerCase())
    );
  }

  onPresentStateChange() {
    this.applicant.presentDistrict = '';
    this.customPresentDistrict = '';
    this.presentDistricts = STATE_DISTRICTS_MAP[this.applicant.presentState] || [];
    this.filteredPresentDistricts = [...this.presentDistricts];
  }

  onPermanentStateChange() {
    this.applicant.permanentDistrict = '';
    this.customPermanentDistrict = '';
    this.permanentDistricts = STATE_DISTRICTS_MAP[this.applicant.permanentState] || [];
    this.filteredPermanentDistricts = [...this.permanentDistricts];
  }

  onPresentStateSelected(state: string) {
    this.applicant.presentState = state;
    this.onPresentStateChange();
  }

  onPermanentStateSelected(state: string) {
    this.applicant.permanentState = state;
    this.onPermanentStateChange();
  }

  onPresentDistrictSelected(district: string) {
    this.applicant.presentDistrict = district;
  }

  onPermanentDistrictSelected(district: string) {
    this.applicant.permanentDistrict = district;
  }

  copyPresentAddress() {
    if (this.applicant.sameAddress) {
      this.applicant.permanentAddress = this.applicant.presentAddress;
      this.applicant.permanentState = this.applicant.presentState;
      this.permanentDistricts = [...this.presentDistricts];
      this.filteredPermanentDistricts = [...this.presentDistricts];
      this.applicant.permanentDistrict = this.applicant.presentDistrict;
      this.customPermanentDistrict = this.customPresentDistrict;
      this.applicant.permanentPinCode = this.applicant.presentPinCode;
    }
    else {
      this.applicant.permanentAddress = '';
      this.applicant.permanentState = '';
      this.permanentDistricts = [];
      this.filteredPermanentDistricts = [];
      this.applicant.permanentDistrict = '';
      this.customPermanentDistrict = '';
      this.applicant.permanentPinCode = '';
    }
  }
  addTraveller() {

    this.coTravellers.push({
      name: '',
      age: '',
      gender: '',
      comorbidities: [] as string[]
    });

  }

  // Delete Co Traveller
  deleteTraveller(index: number) {

    this.coTravellers.splice(index, 1);

  }
  nextStep() {
    if (this.currentStep === 1 && this.isStep1Valid()) {
      this.currentStep = 2;
    } else if (this.currentStep === 2 && this.isStep2Valid()) {
      this.currentStep = 3;
    } else if (this.currentStep === 3 && this.isStep3Valid()) {
      this.currentStep = 4;
    } else if (this.currentStep === 4 && this.isStep4Valid()) {
      this.currentStep = 5;
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  goToStep(step: number) {
    if (step === 1) {
      this.currentStep = 1;
    } else if (step === 2 && this.isStep1Valid()) {
      this.currentStep = 2;
    } else if (step === 3 && this.isStep1Valid() && this.isStep2Valid()) {
      this.currentStep = 3;
    } else if (step === 4 && this.isStep1Valid() && this.isStep2Valid() && this.isStep3Valid()) {
      this.currentStep = 4;
    } else if (step === 5 && this.isStep1Valid() && this.isStep2Valid() && this.isStep3Valid() && this.isStep4Valid()) {
      this.currentStep = 5;
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  submitForm() {
    if (this.isSubmitting) return;

    if (!this.selectedAadharFile || !this.applicant.aadharPhotoPath) {
      this.aadharFileError = 'Aadhaar photo is required and must be uploaded';
      return;
    }

    this.isSubmitting = true;

    const comorbiditiesStr = this.applicant.comorbidities && this.applicant.comorbidities.length > 0
      ? this.applicant.comorbidities.join(', ')
      : 'None';

    const payload = { 
      ...this.applicant,
      comorbidities: comorbiditiesStr
    };

    const serializedCoTravellers = this.coTravellers.map(t => {
      const coComorbiditiesStr = t.comorbidities && t.comorbidities.length > 0
        ? t.comorbidities.join(', ')
        : 'None';
      return {
        name: t.name,
        age: t.age,
        gender: t.gender,
        comorbidities: coComorbiditiesStr
      };
    });

    payload.coTraveller = JSON.stringify(serializedCoTravellers);
    
    if (payload.presentDistrict === 'Other') {
      payload.presentDistrict = this.customPresentDistrict;
    }
    if (payload.permanentDistrict === 'Other') {
      payload.permanentDistrict = this.customPermanentDistrict;
    }

    console.log(payload);
    this.applicantService.register(payload)
      .subscribe({
        next: (res:any) => {
          this.isRegistrationSuccess = true;
          this.isSubmitting = false;
          console.log(res);

          if (typeof window !== 'undefined' && window.sessionStorage) {
            try {
              sessionStorage.setItem('lastRegistration', JSON.stringify({
                applicant: res,
                coTravellers: serializedCoTravellers
              }));
            } catch (e) {
              console.error('Failed to write lastRegistration to sessionStorage:', e);
            }
          }

          this.router.navigate(['/pdfDownload'],{
            queryParams: {
              id: res.id
            },
            state: {
              applicant: res,
              coTravellers: serializedCoTravellers
            }
          })
        },
        error: (err) => {
          this.isSubmitting = false;
          console.log(err);
        }
      });
  }


}
