import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ApplicantService {

  private get baseUrl(): string {
    if (typeof window !== 'undefined') {
      return `http://${window.location.hostname}:8081`;
    }
    return 'http://localhost:8081';
  }

  get apiUrl(): string {
    return `${this.baseUrl}/api/auth`;
  }

  constructor(private http: HttpClient) {}

  register(data: any) {
    return this.http.post(`${this.apiUrl}/register`, data);
  }

  uploadAadharPhoto(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.apiUrl}/upload-aadhar`, formData);
  }

  deleteAadharPhoto(filePath: string) {
    return this.http.post<any>(`${this.apiUrl}/delete-aadhar`, { filePath });
  }


  private getHeaders() {
    let headers: any = {};
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const role = sessionStorage.getItem('admin_role');
      if (role) {
        headers['X-Admin-Role'] = role;
      }
    }
    return headers;
  }

  submitForm(id: any) {
    return this.http.get(`${this.apiUrl}/generate-pdf/${id}`, {
      responseType: 'blob',
      headers: this.getHeaders()
    });
  }

  searchRegistration(mobileNo: any) {
    return this.http.get<any[]>(`${this.apiUrl}/mobile/${mobileNo}`);
  }

  getAllRegistrations() {
    return this.http.get<any[]>(`${this.apiUrl}/registrations`, { headers: this.getHeaders() });
  }

  deleteRegistration(id: any) {
    return this.http.delete(`${this.apiUrl}/registrations/${id}`, { headers: this.getHeaders() });
  }

  resendRegistrationEmail(id: any) {
    return this.http.post<any>(`${this.apiUrl}/registrations/${id}/resend-email`, {}, { headers: this.getHeaders() });
  }

  search(query: string) {
    return this.http.get<any[]>(`${this.baseUrl}/api/auth/search`, {
      params: { query },
      headers: this.getHeaders()
    });
  }

  sendOtp(email: string) {
    return this.http.post<any>(`${this.apiUrl}/otp/send`, {}, {
      params: { email }
    });
  }

  verifyOtp(email: string, otp: string) {
    return this.http.post<any[]>(`${this.apiUrl}/otp/verify`, {}, {
      params: { email, otp }
    });
  }


  sendInquiry(data: any) {
    return this.http.post(`${this.baseUrl}/api/inquiries`, data);
  }

  getAllInquiries() {
    return this.http.get<any[]>(`${this.baseUrl}/api/inquiries`, { headers: this.getHeaders() });
  }

  deleteInquiry(id: any) {
    return this.http.delete(`${this.baseUrl}/api/inquiries/${id}`, { headers: this.getHeaders() });
  }

  disposeInquiryWithMessage(id: any, message: string) {
    return this.http.post(`${this.baseUrl}/api/inquiries/${id}/dispose`, { message }, { headers: this.getHeaders() });
  }

  adminLogin(credentials: any) {
    return this.http.post<any>(`${this.baseUrl}/api/admin/login`, credentials);
  }

  getAdminMembers() {
    return this.http.get<any[]>(`${this.baseUrl}/api/admin/members`, { headers: this.getHeaders() });
  }

  addAdminMember(memberData: any) {
    return this.http.post<any>(`${this.baseUrl}/api/admin/members`, memberData, { headers: this.getHeaders() });
  }

  updateAdminMember(id: any, memberData: any) {
    return this.http.put<any>(`${this.baseUrl}/api/admin/members/${id}`, memberData, { headers: this.getHeaders() });
  }

  deleteAdminMember(id: any) {
    return this.http.delete<any>(`${this.baseUrl}/api/admin/members/${id}`, { headers: this.getHeaders() });
  }

  fetchStates(){
    const body = {
      country: 'India'
    };
    return this.http.post<any>(
      'https://countriesnow.space/api/v0.1/countries/states',
      body
    )
  }
  
}
