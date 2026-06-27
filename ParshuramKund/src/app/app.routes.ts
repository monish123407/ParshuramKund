import { Routes } from '@angular/router';
import { HowToReach } from './components/how-to-reach/how-to-reach';
import { Home } from './components/home/home';
import { Contactus } from './components/contactus/contactus';
import { Gallery } from './components/gallery/gallery';
import { Register } from './components/register/register';
import { PdfViewer } from './components/pdf-viewer/pdf-viewer';
import { RegistrationPdfDownload } from './components/registration-pdf-download/registration-pdf-download';
import { TestComponent } from './components/test/test';
import { Admin } from './components/admin/admin';
import { Aboutus } from './components/aboutus/aboutus';

export const routes: Routes = [
    {
        path: '',
        component: Home
      },
    {
        path: 'how-to-reach',
        component: HowToReach
      },
      {
        path: 'contact',
        component: Contactus
      },
      {
        path: 'about',
        component: Aboutus
      },
      {
        path: 'registration',
        component: Register
      },
      {
        path: 'pdfDownload',
        component: PdfViewer
      },
      {
        path: 'registrationpdf',
        component: RegistrationPdfDownload
      },
      {
        path: 'test',
        component: TestComponent
      },
      {
        path: 'gallery',
        component: Gallery
      },
      {
        path: 'admin',
        component: Admin
      },
      {
        path: '**',
        redirectTo: ''
      }
];
