import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationPdfDownload } from './registration-pdf-download';

describe('RegistrationPdfDownload', () => {
  let component: RegistrationPdfDownload;
  let fixture: ComponentFixture<RegistrationPdfDownload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrationPdfDownload],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationPdfDownload);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
